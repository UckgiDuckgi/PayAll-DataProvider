package com.example.PayAll_DataProvider.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountBasicInfoDto {
	private String accountNum;        // 계좌번호
	private String baseDate;          // 기준일자
	private String currencyCode;      // 통화 코드
	private BigDecimal withholdingsAmt; // 예수금
	private BigDecimal creditLoanAmt;  // 신용대출금
	private BigDecimal mortgageAmt;    // 담보대출금
	private BigDecimal availBalance;   // 출금가능금액
}
