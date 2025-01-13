package com.example.PayAll_DataProvider.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
	private String accountNum; // 계좌번호
	private boolean isConsent; // 전송 요구 여부
	private String accountName; // 계좌명
	private String accountType; // 계좌종류
	private String issueDate; // 계좌개설일
	private boolean isTaxBenefits; // 세제혜택 적용여부
	private boolean isCma; // CMA 상품 포함 여부
	private boolean isStockTrans; // 주식거래 가능 여부
	private boolean isAccountLink; // 은행예수금 방식 제공 여부
}
