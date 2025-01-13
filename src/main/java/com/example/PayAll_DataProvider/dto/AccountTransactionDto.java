package com.example.PayAll_DataProvider.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountTransactionDto {
	private String prodName;         // 상품명
	private String prodCode;         // 상품 코드
	private LocalDateTime transDtime; // 거래 일시
	private String transNo;          // 거래 번호
	private String transType;        // 거래 유형 코드
	private String transTypeDetail;  // 거래 유형 상세
	private BigDecimal transNum;     // 거래 수량
	private String transUnit;        // 거래 단위
}
