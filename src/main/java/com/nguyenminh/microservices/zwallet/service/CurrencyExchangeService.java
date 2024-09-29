package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.CurrencyCaculate;
import com.nguyenminh.microservices.zwallet.dto.CurrencyExchangeRequest;
import com.nguyenminh.microservices.zwallet.model.CurrencyExchange;
import com.nguyenminh.microservices.zwallet.repository.CurrencyExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {


    private final CurrencyExchangeRepository currencyExchangeRepository;

    // Create new currency exchange
    public CurrencyExchange createCurrencyExchange(CurrencyExchange currencyExchange) {
        return currencyExchangeRepository.save(currencyExchange);
    }

    // Get all currency exchanges
    public List<CurrencyExchange> getAllCurrencyExchanges() {
        return currencyExchangeRepository.findAll();
    }

    // Get currency exchange by ID
    public Optional<CurrencyExchange> getCurrencyExchangeById(String id) {
        return currencyExchangeRepository.findById(id);
    }

    // Update existing currency exchange
    public CurrencyExchange updateCurrencyExchange(String id, CurrencyExchange updatedCurrencyExchange) {
        Optional<CurrencyExchange> existingExchange = currencyExchangeRepository.findById(id);
        if (existingExchange.isPresent()) {
            CurrencyExchange currencyExchange = existingExchange.get();
            currencyExchange.setCurrency_from(updatedCurrencyExchange.getCurrency_from());
            currencyExchange.setCurrency_to(updatedCurrencyExchange.getCurrency_to());
            currencyExchange.setConversionMultiple(updatedCurrencyExchange.getConversionMultiple());
            return currencyExchangeRepository.save(currencyExchange);
        } else {
            return null;  // Or throw an exception
        }
    }

    public ResponseEntity<?> currencyCaculate(CurrencyExchangeRequest currencyExchangeRequest) {
        List<CurrencyExchange> currencyExchanges = currencyExchangeRepository.findAll();

        // Loop through the list and find a match for 'from' and 'to' currency
        Optional<CurrencyExchange> matchingExchange = currencyExchanges.stream()
                .filter(currencyExchange ->
                        currencyExchange.getCurrency_from().equals(currencyExchangeRequest.getFrom()) &&
                                currencyExchange.getCurrency_to().equals(currencyExchangeRequest.getTo())
                )
                .findFirst();

        // If a matching exchange is found, return the conversion details
        if (matchingExchange.isPresent()) {
            CurrencyExchange exchange = matchingExchange.get();
            BigDecimal conversionMultiple = exchange.getConversionMultiple();

            // Calculate the converted amount
            BigDecimal convertedAmount = currencyExchangeRequest.getAmount()
                    .multiply(conversionMultiple);

            // Return the calculation result wrapped in a response object
            CurrencyCaculate result = new CurrencyCaculate();
            result.setResult(convertedAmount);
            return ResponseEntity.ok(result);
        } else {
            // If no match is found, throw an exception or handle error accordingly
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
    // Delete currency exchange by ID
    public void deleteCurrencyExchange(String id) {
        currencyExchangeRepository.deleteById(id);
    }



}
