package com.example.openbook.kakaopay;

import com.google.gson.annotations.SerializedName;

public class KakaoPayApproveResponseDTO {
    String tid;
    String cid;
    @SerializedName("partner_order_id")
    String partnerOrderId;
    @SerializedName("partner_user_id")
    String partnerUserId;
    @SerializedName("payment_method_type")
    String paymentMethodType;

    Amount amount;
    @SerializedName("item_name")
    String itemName;
    String quantity;
    @SerializedName("approved_at")
    String approvedAt;

    public String getApprovedAt() {
        return approvedAt;
    }

    public class Amount{
        String total;
    }
}
