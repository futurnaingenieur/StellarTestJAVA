package com.teststellar;

import org.stellar.sdk.*;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import static com.teststellar.Main.*;

public class PaymentListenerJob{

    public static void main(String[] args) {
        KeyPair mikel = KeyPair.fromSecretSeed(AccountStore.DESTINATION_PRIVATE_KEY);

        //Now we'll read some information of the new account
        Network.useTestNetwork();
        Server server = new Server("https://horizon-testnet.stellar.org");
        // Create an API call to query payments involving the account.
        PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(mikel);

        // `stream` will send each recorded payment, one by one, then keep the
        // connection open and continue to send you new payments as they occur.
        paymentsRequest.stream(new EventListener<OperationResponse>() {
            @Override
            public void onEvent(OperationResponse payment) {
                // Record the paging token so we can start from here next time.
                //savePagingToken(payment.getPagingToken());

                // The payments stream includes both sent and received payments. We only
                // want to process received payments here.
                if (payment instanceof PaymentOperationResponse) {
                    if (((PaymentOperationResponse) payment).getTo().equals(mikel)) {
                        return;
                    }

                    String amount = ((PaymentOperationResponse) payment).getAmount();

                    Asset asset = ((PaymentOperationResponse) payment).getAsset();
                    String assetName;
                    if (asset.equals(new AssetTypeNative())) {
                        assetName = "lumens";
                    } else {
                        StringBuilder assetNameBuilder = new StringBuilder();
                        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getCode());
                        assetNameBuilder.append(":");
                        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getIssuer().getAccountId());
                        assetName = assetNameBuilder.toString();
                    }

                    StringBuilder output = new StringBuilder();
                    output.append(amount);
                    output.append(" ");
                    output.append(assetName);
                    output.append(" from ");
                    output.append(((PaymentOperationResponse) payment).getFrom().getAccountId());
                    System.out.println(output.toString());
                }

            }
        });

        while(true);

    }
}
