package com.rexlite.rexlitebasicnew.barcode;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.rexlite.rexlitebasicnew.GuideActivity;

public final class ResultHandlerFactory {
    private ResultHandlerFactory() {
    }

    public static ResultHandler makeResultHandler(GuideActivity activity, Result rawResult) {
        ParsedResult result = parseResult(rawResult);
        switch (result.getType()) {
            case WIFI:
                return new WifiResultHandler(activity, result);
            default:
                return new WifiResultHandler(activity, result);
        }
    }

    private static ParsedResult parseResult(Result rawResult) {
        return ResultParser.parseResult(rawResult);
    }
}
