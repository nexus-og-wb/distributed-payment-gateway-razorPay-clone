package com.prashant.razorpay.merchant_service.entity;

import com.prashant.razorpay.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "merchant_webhook_config",
indexes = {@Index(name = "idx_webhook_merchant_id", columnList = "merchant_id, enabled")
})
public class MerchantWebhookConfig extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 500)
    private String targetUrl;

    @Column(length = 255)
    private String eventTypes;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 255)
    private String webhookSecret;


    public boolean isSubscribedTo(String eventType) {
        if(eventType == null || eventTypes.isBlank()) {
            return true;
        }
        for(String type : eventTypes.split(",")) {
            String trimmed = type.trim();
            if(trimmed.equalsIgnoreCase("ALL") || trimmed.equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }

}
