package com.teststellar;

import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;

public class Main {

    public static void main(String[] args) throws Exception {

        KeyPair mehdi = KeyPair.fromSecretSeed(AccountStore.SOURCE_PRIVATE_KEY);

        KeyPair issuer = KeyPair.fromSecretSeed(AccountStore.ISSUER_PRIVATE_KEY);

        //Now we'll read some information of the new account
        Network.useTestNetwork();
        Server server = new Server("https://horizon-testnet.stellar.org");

        System.out.println("Issuer Before transaction");
        AccountUtil.printAccountBalances(AccountUtil.getTestAccount(issuer));

        System.out.println("Mehdi Before transaction");
        AccountUtil.printAccountBalances(AccountUtil.getTestAccount(mehdi));

        AccountUtil.sendAssetFromAccountToAccount(issuer, mehdi, 15000, new AssetTypeNative(), server);

        System.out.println("Issuer After transaction");
        AccountUtil.printAccountBalances(AccountUtil.getTestAccount(issuer));

        System.out.println("Mehdi After transaction");
        AccountUtil.printAccountBalances(AccountUtil.getTestAccount(mehdi));

        Asset herozMarilyn = Asset.createNonNativeAsset("HerozMarilyn", issuer);

        AccountResponse mehdiAccount = AccountUtil.getTestAccount(mehdi);

        // First, the receiving account must trust the asset
        Transaction allowMarilyn = new Transaction.Builder(mehdiAccount)
                .addOperation(
                        // The `ChangeTrust` operation creates (or alters) a trustline
                        // The second parameter limits the amount the account can hold
                        new ChangeTrustOperation.Builder(herozMarilyn, "1").build())
                .build();
        allowMarilyn.sign(mehdi);
        server.submitTransaction(allowMarilyn);


        // Second, the issuing account actually sends a payment using the asset
        AccountResponse issuing = server.accounts().account(issuer);
        Transaction sendMarilyn = new Transaction.Builder(issuing)
                .addOperation(
                        new PaymentOperation.Builder(mehdi, herozMarilyn, "1").build())
                .build();
        sendMarilyn.sign(issuer);
        server.submitTransaction(sendMarilyn);

        System.out.println("Transaction Effectuee Successfully !!!");

        AccountUtil.printAccountBalances(AccountUtil.getTestAccount(mehdi));

    }

}
