package com.roomate.app.service.implementation;

import com.roomate.app.dto.RoomMemberDto;
import com.roomate.app.dto.ledger.*;
import com.roomate.app.entities.ledger.*;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.*;
import com.roomate.app.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerSplitRepository ledgerSplitRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    @Override
    @Transactional
    public LedgerEntryDto createLedgerEntry(LedgerEntryCreateDto dto, String userEmail) {
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new UserApiError("Room not found"));

        RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(dto.getRoomId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only landlords and head roommates can create ledger entries
        if (member.getRole() != RoomMemberEnum.LANDLORD && member.getRole() != RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Only landlords and head roommates can create ledger entries");
        }

        LedgerEntryEntity entry = new LedgerEntryEntity(
                room,
                member,
                dto.getTitle(),
                dto.getEntryType(),
                dto.getTotalAmount(),
                dto.getSplitType());
        entry.setDescription(dto.getDescription());
        entry.setDueDate(dto.getDueDate());

        LedgerEntryEntity saved = ledgerEntryRepository.save(entry);
        return LedgerEntryDto.fromEntity(saved);
    }

    @Override
    public List<LedgerEntryDto> getLedgerEntriesForRoom(UUID roomId, String userEmail) {
        validateRoomMembership(roomId, userEmail);

        return ledgerEntryRepository.findActiveByRoomId(roomId).stream()
                .map(LedgerEntryDto::fromEntity)
                .toList();
    }

    @Override
    public LedgerEntryDto getLedgerEntryById(UUID entryId, String userEmail) {
        LedgerEntryEntity entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Ledger entry not found"));

        validateRoomMembership(entry.getRoom().getId(), userEmail);
        return LedgerEntryDto.fromEntity(entry);
    }

    @Override
    @Transactional
    public LedgerEntryDto assignSplits(UUID entryId, AssignSplitsDto dto, String userEmail) {
        LedgerEntryEntity entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Ledger entry not found"));

        RoomMemberEntity assigningMember = roomMemberRepository
                .findByRoomIdAndUserEmail(entry.getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only head roommates can assign splits
        if (assigningMember.getRole() != RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Only head roommates can assign expense splits");
        }

        // Clear existing splits
        entry.getSplits().clear();

        // Create new splits
        BigDecimal totalAssigned = BigDecimal.ZERO;
        for (AssignSplitsDto.SplitAssignment assignment : dto.getAssignments()) {
            RoomMemberEntity member = roomMemberRepository.findById(assignment.getMemberId())
                    .orElseThrow(() -> new UserApiError("Member not found: " + assignment.getMemberId()));

            LedgerSplitEntity split = new LedgerSplitEntity(entry, member, assignment.getAmount());
            split.setNotes(assignment.getNotes());
            entry.getSplits().add(split);
            totalAssigned = totalAssigned.add(assignment.getAmount());
        }

        // Validate total matches entry amount
        if (totalAssigned.compareTo(entry.getTotalAmount()) != 0) {
            throw new UserApiError("Split amounts must equal total amount. Expected: " +
                    entry.getTotalAmount() + ", Got: " + totalAssigned);
        }

        entry.setStatus(LedgerEntryStatus.APPROVED);
        LedgerEntryEntity saved = ledgerEntryRepository.save(entry);
        return LedgerEntryDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public LedgerEntryDto calculateEqualSplits(UUID entryId, String userEmail) {
        LedgerEntryEntity entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Ledger entry not found"));

        RoomMemberEntity assigningMember = roomMemberRepository
                .findByRoomIdAndUserEmail(entry.getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only head roommates can assign splits
        if (assigningMember.getRole() != RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Only head roommates can assign expense splits");
        }

        // Get all room members (excluding landlords from split)
        List<RoomMemberEntity> members = roomMemberRepository.findByRoomID(entry.getRoom().getId())
                .stream()
                .filter(m -> m.getRole() != RoomMemberEnum.LANDLORD && m.getRole() != RoomMemberEnum.GUEST)
                .toList();

        if (members.isEmpty()) {
            throw new UserApiError("No members available to split the expense");
        }

        // Clear existing splits
        entry.getSplits().clear();

        // Calculate equal split amount
        BigDecimal splitAmount = entry.getTotalAmount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        // Handle rounding difference
        BigDecimal totalAssigned = splitAmount.multiply(BigDecimal.valueOf(members.size()));
        BigDecimal remainder = entry.getTotalAmount().subtract(totalAssigned);

        for (int i = 0; i < members.size(); i++) {
            BigDecimal amount = splitAmount;
            // Add remainder to first member's split
            if (i == 0 && remainder.compareTo(BigDecimal.ZERO) != 0) {
                amount = amount.add(remainder);
            }
            LedgerSplitEntity split = new LedgerSplitEntity(entry, members.get(i), amount);
            entry.getSplits().add(split);
        }

        entry.setStatus(LedgerEntryStatus.APPROVED);
        entry.setSplitType(SplitType.EQUAL);
        LedgerEntryEntity saved = ledgerEntryRepository.save(entry);
        return LedgerEntryDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public LedgerSplitDto recordPayment(UUID splitId, RecordPaymentDto dto, String userEmail) {
        LedgerSplitEntity split = ledgerSplitRepository.findById(splitId)
                .orElseThrow(() -> new UserApiError("Split not found"));

        RoomMemberEntity payingMember = roomMemberRepository.findByRoomIdAndUserEmail(
                split.getLedgerEntry().getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only the member who owes can record their own payment, or head roommate can
        // record for others
        boolean isOwnPayment = split.getRoomMember().getId().equals(payingMember.getId());
        boolean isHeadRoommate = payingMember.getRole() == RoomMemberEnum.HEAD_ROOMMATE;

        if (!isOwnPayment && !isHeadRoommate) {
            throw new UserApiError("You can only record your own payments");
        }

        split.recordPayment(dto.getAmount());
        if (dto.getNotes() != null && !dto.getNotes().isEmpty()) {
            split.setNotes(dto.getNotes());
        }

        // Update entry status if needed
        LedgerEntryEntity entry = split.getLedgerEntry();
        if (entry.isFullyPaid()) {
            entry.setStatus(LedgerEntryStatus.PAID);
        } else if (entry.getTotalPaid().compareTo(BigDecimal.ZERO) > 0) {
            entry.setStatus(LedgerEntryStatus.PARTIALLY_PAID);
        }

        ledgerEntryRepository.save(entry);
        LedgerSplitEntity saved = ledgerSplitRepository.save(split);
        return LedgerSplitDto.fromEntity(saved);
    }

    @Override
    public List<MemberBalanceDto> getMemberBalances(UUID roomId, String userEmail) {
        validateRoomMembership(roomId, userEmail);

        List<RoomMemberEntity> members = roomMemberRepository.findByRoomID(roomId);
        List<MemberBalanceDto> balances = new ArrayList<>();

        for (RoomMemberEntity member : members) {
            // Skip landlords (they don't owe, they receive)
            if (member.getRole() == RoomMemberEnum.LANDLORD) {
                continue;
            }

            List<LedgerSplitEntity> splits = ledgerSplitRepository.findByMemberIdAndRoomId(member.getId(), roomId);

            BigDecimal totalOwed = splits.stream()
                    .map(LedgerSplitEntity::getAmountOwed)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPaid = splits.stream()
                    .map(LedgerSplitEntity::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int unpaidCount = (int) splits.stream()
                    .filter(s -> s.getPaymentStatus() != PaymentStatus.PAID)
                    .count();

            balances.add(MemberBalanceDto.builder()
                    .memberId(member.getId())
                    .member(RoomMemberDto.fromEntity(member))
                    .totalOwed(totalOwed)
                    .totalPaid(totalPaid)
                    .outstandingBalance(totalOwed.subtract(totalPaid))
                    .unpaidSplitsCount(unpaidCount)
                    .build());
        }

        return balances;
    }

    @Override
    public MemberBalanceDto getMemberBalance(UUID roomId, UUID memberId, String userEmail) {
        validateRoomMembership(roomId, userEmail);

        RoomMemberEntity member = roomMemberRepository.findById(memberId)
                .orElseThrow(() -> new UserApiError("Member not found"));

        List<LedgerSplitEntity> splits = ledgerSplitRepository.findByMemberIdAndRoomId(memberId, roomId);

        BigDecimal totalOwed = splits.stream()
                .map(LedgerSplitEntity::getAmountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = splits.stream()
                .map(LedgerSplitEntity::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int unpaidCount = (int) splits.stream()
                .filter(s -> s.getPaymentStatus() != PaymentStatus.PAID)
                .count();

        return MemberBalanceDto.builder()
                .memberId(member.getId())
                .member(RoomMemberDto.fromEntity(member))
                .totalOwed(totalOwed)
                .totalPaid(totalPaid)
                .outstandingBalance(totalOwed.subtract(totalPaid))
                .unpaidSplitsCount(unpaidCount)
                .build();
    }

    @Override
    @Transactional
    public void cancelLedgerEntry(UUID entryId, String userEmail) {
        LedgerEntryEntity entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Ledger entry not found"));

        RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(entry.getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only head roommates or the creator can cancel
        boolean isHeadRoommate = member.getRole() == RoomMemberEnum.HEAD_ROOMMATE;
        boolean isCreator = entry.getCreatedBy() != null && entry.getCreatedBy().getId().equals(member.getId());

        if (!isHeadRoommate && !isCreator) {
            throw new UserApiError("You don't have permission to cancel this entry");
        }

        entry.setStatus(LedgerEntryStatus.CANCELLED);
        ledgerEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void deleteLedgerEntry(UUID entryId, String userEmail) {
        LedgerEntryEntity entry = ledgerEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Ledger entry not found"));

        RoomMemberEntity member = roomMemberRepository.findByRoomIdAndUserEmail(entry.getRoom().getId(), userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));

        // Only head roommates can delete
        if (member.getRole() != RoomMemberEnum.HEAD_ROOMMATE) {
            throw new UserApiError("Only head roommates can delete ledger entries");
        }

        ledgerEntryRepository.delete(entry);
    }

    private RoomMemberEntity validateRoomMembership(UUID roomId, String userEmail) {
        return roomMemberRepository.findByRoomIdAndUserEmail(roomId, userEmail)
                .orElseThrow(() -> new UserApiError("You are not a member of this room"));
    }
}
