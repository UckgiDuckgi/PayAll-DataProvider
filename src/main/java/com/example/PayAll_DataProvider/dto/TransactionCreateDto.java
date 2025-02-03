package com.example.PayAll_DataProvider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionCreateDto {
	private String storeName;
	private Long price;
}
