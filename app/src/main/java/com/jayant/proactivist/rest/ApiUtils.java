package com.jayant.proactivist.rest;

public class ApiUtils {

    private ApiUtils() {}

    public static CompanySearchAPIService getCompanySearchAPIService() {
        return RetrofitClient.getCompanySearchClient().create(CompanySearchAPIService.class);
    }

    public static APIService getAPIService() {
        return RetrofitClient.getClient().create(APIService.class);
    }
}