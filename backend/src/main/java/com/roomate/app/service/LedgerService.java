package com.roomate.app.service;

import com.roomate.app.dto.ledger.*;

import java.util.List;
import java.util.UUID;

public interface LedgerService {

    LedgerEntryDto createLedgerEntry(LedgerEntryCreateDto dto, String userEmail);

    List<LedgerEntryDto> getLedgerEntriesForRoom(UUID roomId, String userEmail);

    LedgerEntryDto getLedgerEntryById(UUID entryId, String userEmail);

    LedgerEntryDto assignSplits(UUID entryId, AssignSplitsDto dto, String userEmail);

    LedgerEntryDto calculateEqualSplits(UUID entryId, String userEmail);

    LedgerSplitDto recordPayment(UUID splitId, RecordPaymentDto dto, String userEmail);

    List<MemberBalanceDto> getMemberBalances(UUID roomId, String userEmail);

    MemberBalanceDto getMemberBalance(UUID roomId, UUID memberId, String userEmail);

    void cancelLedgerEntry(UUID entryId, String userEmail);

    void deleteLedgerEntry(UUID entryId, String userEmail);
}
