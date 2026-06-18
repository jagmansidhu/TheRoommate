package com.roomate.app.service.implementation;

import com.roomate.app.dto.BudgetEntryDto;
import com.roomate.app.dto.BudgetSettingsDto;
import com.roomate.app.dto.BudgetStatsDto;
import com.roomate.app.dto.CreateBudgetEntryRequest;
import com.roomate.app.entities.budget.BudgetEntryEntity;
import com.roomate.app.entities.budget.UserBudgetEntity;
import com.roomate.app.exceptions.UserApiError;
import com.roomate.app.repository.budget.BudgetEntryRepository;
import com.roomate.app.repository.budget.ReceiptStorageRepository;
import com.roomate.app.repository.budget.UserBudgetRepository;
import com.roomate.app.service.BudgetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Value("${n8n.webhook.url:}")
    private String n8nWebhookUrl;

    @Value("${n8n.webhook.token:}")
    private String n8nWebhookToken;

    private final BudgetEntryRepository budgetEntryRepository;
    private final UserBudgetRepository userBudgetRepository;
    private final ReceiptStorageRepository receiptStorageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public BudgetServiceImpl(BudgetEntryRepository budgetEntryRepository,
                             UserBudgetRepository userBudgetRepository,
                             ReceiptStorageRepository receiptStorageRepository) {
        this.budgetEntryRepository = budgetEntryRepository;
        this.userBudgetRepository = userBudgetRepository;
        this.receiptStorageRepository = receiptStorageRepository;
    }

    private BudgetEntryDto mapToDto(BudgetEntryEntity entity) {
        String s3Url = receiptStorageRepository.findByReceiptId(entity.getId())
                .map(storage -> storage.getFileUrl())
                .orElse(null);

        return new BudgetEntryDto(
                entity.getId(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getStatus(),
                s3Url,
                entity.getPaymentDate(),
                entity.getSubmittedAt()
        );
    }

    @Override
    @Transactional("budgetTransactionManager")
    public List<BudgetEntryDto> getEntries(String email) {
        return budgetEntryRepository.findByUserIdOrderBySubmittedAtDesc(email).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("budgetTransactionManager")
    public List<BudgetEntryDto> getEntriesByMonth(String email, int year, int month) {
        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        return budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                        email, startOfMonth.toInstant(), endOfMonth.toInstant()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetStatsDto getStats(String email, int year, int month) {
        List<BudgetEntryEntity> entries;
        if (year > 0 && month > 0) {
            ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
            entries = budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                    email, startOfMonth.toInstant(), endOfMonth.toInstant());
        } else {
             // Default to current month if not specified
             ZonedDateTime startOfMonth = ZonedDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
             ZonedDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
             entries = budgetEntryRepository.findByUserIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                     email, startOfMonth.toInstant(), endOfMonth.toInstant());
        }

        BigDecimal totalSpent = entries.stream()
                .map(BudgetEntryEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> spentByCategory = entries.stream()
                .collect(Collectors.groupingBy(
                        BudgetEntryEntity::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, BudgetEntryEntity::getAmount, BigDecimal::add)
                ));

        BigDecimal monthlyBudget = userBudgetRepository.findByUserId(email)
                .map(UserBudgetEntity::getMonthlyBudget)
                .orElse(BigDecimal.ZERO);

        BigDecimal remainingBudget = monthlyBudget.subtract(totalSpent);

        return new BudgetStatsDto(totalSpent, spentByCategory, monthlyBudget, remainingBudget);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetEntryDto createEntry(String email, CreateBudgetEntryRequest request) {
        BudgetEntryEntity entity = new BudgetEntryEntity(
                email,
                request.amount,
                request.category != null ? request.category : "Other",
                request.description,
                request.status,
                request.paymentDate != null ? request.paymentDate : Instant.now()
        );
        BudgetEntryEntity saved = budgetEntryRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void updateEntryCategory(UUID entryId, String email, String newCategory) {
        BudgetEntryEntity entity = budgetEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Entry not found"));
        
        if (!entity.getUserId().equals(email)) {
            throw new UserApiError("Unauthorized to update this entry");
        }

        entity.setCategory(newCategory);
        budgetEntryRepository.save(entity);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void deleteEntry(UUID entryId, String email) {
        BudgetEntryEntity entity = budgetEntryRepository.findById(entryId)
                .orElseThrow(() -> new UserApiError("Entry not found"));

        if (!entity.getUserId().equals(email)) {
            throw new UserApiError("Unauthorized to delete this entry");
        }

        budgetEntryRepository.delete(entity);
    }

    @Override
    @Transactional("budgetTransactionManager")
    public BudgetSettingsDto getBudgetSettings(String email) {
        BudgetSettingsDto dto = new BudgetSettingsDto();
        dto.monthlyBudget = userBudgetRepository.findByUserId(email)
                .map(UserBudgetEntity::getMonthlyBudget)
                .orElse(BigDecimal.ZERO);
        return dto;
    }

    @Override
    @Transactional("budgetTransactionManager")
    public void setBudgetSettings(String email, BudgetSettingsDto settings) {
        UserBudgetEntity entity = userBudgetRepository.findByUserId(email)
                .orElse(new UserBudgetEntity(email, settings.monthlyBudget));
        
        entity.setMonthlyBudget(settings.monthlyBudget);
        userBudgetRepository.save(entity);
    }

    @Override
    public void uploadReceipts(String email, List<MultipartFile> files) {
        if (!StringUtils.hasText(n8nWebhookUrl)) {
            throw new UserApiError("Receipt upload is not configured on the server.");
        }
        if (files == null || files.isEmpty()) {
            throw new UserApiError("No files provided.");
        }

        try {
            // Append userId as a query param — n8n reads it as {{ $json.query.userId }}
            String url = n8nWebhookUrl + (n8nWebhookUrl.contains("?") ? "&" : "?")
                    + "userId=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (StringUtils.hasText(n8nWebhookToken)) {
                headers.set("X-Webhook-Token", n8nWebhookToken);
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            for (MultipartFile file : files) {
                byte[] bytes = file.getBytes();
                String filename = file.getOriginalFilename();
                ByteArrayResource resource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() { return filename; }
                };
                body.add("files[]", resource);
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);


            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new UserApiError("n8n webhook returned status: " + response.getStatusCode());
            }
        } catch (UserApiError e) {
            throw e;
        } catch (Exception e) {
            throw new UserApiError("Failed to forward receipts to processing service: " + e.getMessage());
        }
    }
}
