package com.importservice.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedClientHttpResponse implements ClientHttpResponse {
    
    private final ClientHttpResponse response;
    private final byte[] body;
    
    public BufferedClientHttpResponse(ClientHttpResponse response, byte[] body) {
        this.response = response;
        this.body = body;
    }
    
    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }
    
    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }
    
    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }
    
    @Override
    public void close() {
        response.close();
    }
    
    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(body);
    }
    
    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }
}