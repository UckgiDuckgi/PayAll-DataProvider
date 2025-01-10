package com.example.PayAll_DataProvider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowestPriceDto {
	private String productName;
	private String productImage;
	private int price;
	private String shopImage;
	private String shopName;
	private String shopUrl;
}
