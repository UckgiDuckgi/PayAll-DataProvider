package com.example.PayAll_DataProvider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionCreateDto {
	private String shopName;
	private Long price;
}
