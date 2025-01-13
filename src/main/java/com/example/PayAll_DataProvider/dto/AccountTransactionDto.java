package com.example.PayAll_DataProvider.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountTransactionDto {
	private String prodName;         // 상품명
	private String prodCode;         // 상품 코드
	private Timestamp transDtime;
	private String transNo;          // 거래 번호
	private String transType;        // 거래 유형 코드
	private String transTypeDetail;  // 거래 유형 상세
	private BigDecimal transNum;     // 거래 수량
	private String transUnit;        // 거래 단위
	private BigDecimal baseAmt;       // 거래단가
	private BigDecimal transAmt;      // 거래금액
	private BigDecimal settleAmt;     // 정산금액
	private BigDecimal balanceAmt;    // 거래후 잔액
	private String currencyCode;      // 통화 코드
	private String transMemo;         // 적요
	private String exCode;            // 거래소 코드
}
