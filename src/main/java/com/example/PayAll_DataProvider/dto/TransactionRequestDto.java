package com.example.PayAll_DataProvider.dto;

import java.sql.Timestamp;
import java.time.LocalDate;

import lombok.Data;

@Data
public class TransactionRequestDto {
	private String orgCode;
	private String accountNum;
	private Timestamp fromDate;
	private Timestamp toDate;
	private String nextPage;
	private int limit;
}
