package com.nairbspace.octoandroid.data.mapper;

import android.text.TextUtils;

import com.nairbspace.octoandroid.data.db.PrinterDbEntity;
import com.nairbspace.octoandroid.data.exception.EntityMapperException;
import com.nairbspace.octoandroid.data.exception.IncorrectAddPrinterFormattingException;
import com.nairbspace.octoandroid.data.exception.IpAddressEmptyException;
import com.nairbspace.octoandroid.domain.model.AddPrinter;
import com.nairbspace.octoandroid.domain.model.Printer;

import javax.inject.Inject;

import okhttp3.HttpUrl;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

public class PrinterDbEntityMapper {
    private static final String HTTP_SCHEME = "http";
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private static final String HTTPS_SCHEME = "https";

    @Inject
    public PrinterDbEntityMapper() {
    }

    public Func1<PrinterDbEntity, Printer> maptoPrinter() {
        return new Func1<PrinterDbEntity, Printer>() {
            @Override
            public Printer call(PrinterDbEntity printerDbEntity) {
                try {
                    return Printer.builder()
                            .id(printerDbEntity.getId())
                            .name(printerDbEntity.getName())
                            .apiKey(printerDbEntity.getApiKey())
                            .scheme(printerDbEntity.getScheme())
                            .host(printerDbEntity.getHost())
                            .port(printerDbEntity.getPort())
                            .build();
                } catch (Exception e) {
                    throw Exceptions.propagate(new EntityMapperException(e));
                }
            }
        };
    }

    public Observable.OnSubscribe<PrinterDbEntity> mapAddPrinterToPrinterDbEntity(final AddPrinter addPrinter) {
        return new Observable.OnSubscribe<PrinterDbEntity>() {
            @Override
            public void call(Subscriber<? super PrinterDbEntity> subscriber) {
                if (TextUtils.isEmpty(addPrinter.ipAddress())) {
                    subscriber.onError(new IpAddressEmptyException());
                }

                String ipAddress = extractHost(addPrinter.ipAddress());
                String accountName = validateName(ipAddress, addPrinter.accountName());
                int port = convertPortStringToInt(addPrinter.port(), addPrinter.isSslChecked());
                String scheme = convertIsSslCheckedToScheme(addPrinter.isSslChecked());

                PrinterDbEntity printerDbEntity = new PrinterDbEntity();
                printerDbEntity.setName(accountName);
                printerDbEntity.setApiKey(addPrinter.apiKey());
                printerDbEntity.setScheme(scheme);
                printerDbEntity.setHost(ipAddress);
                printerDbEntity.setPort(port);

                if (isUrlValid(printerDbEntity)) {
                    subscriber.onNext(printerDbEntity);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IncorrectAddPrinterFormattingException());
                }
            }
        };
    }

    private String extractHost(String ipAddress) {
        // If user inputted http:// or https:// try to extract only IP Address
        HttpUrl ipAddressUrl = HttpUrl.parse(ipAddress);
        if (ipAddressUrl != null) {
            return ipAddressUrl.host();
        }
        return ipAddress;
    }

    private String validateName(String ipAddress, String name) {
        if (TextUtils.isEmpty(name)) {
            return ipAddress;
        }
        return name;
    }

    private int convertPortStringToInt(String port, boolean isSslChecked) {
        int formattedPortNum;
        try {
            formattedPortNum = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            if (isSslChecked) {
                formattedPortNum = HTTPS_PORT;
            } else {
                formattedPortNum = HTTP_PORT;
            }
        }

        return formattedPortNum;
    }

    private String convertIsSslCheckedToScheme(boolean isSslChecked) {
        return isSslChecked ? HTTPS_SCHEME : HTTP_SCHEME;
    }

    private boolean isUrlValid(PrinterDbEntity printerDbEntity) {
        try {
            new HttpUrl.Builder()
                    .scheme(printerDbEntity.getScheme())
                    .host(printerDbEntity.getHost())
                    .port(printerDbEntity.getPort())
                    .build();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}