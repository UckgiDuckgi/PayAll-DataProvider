package com.example.PayAll_DataProvider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowestPriceDto {
	private Long pCode;
	private String productName;
	private String productImage;
	private Long price;
	private String shopImage;
	private String shopName;
	private String shopUrl;
}
