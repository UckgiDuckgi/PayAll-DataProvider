package com.example.PayAll_DataProvider.service;

import java.io.IOException;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;

public interface CrawlingService {
	void saveProductToRedis();

	LowestPriceDto crawlProductInfo(String pCode) throws IOException;

}
