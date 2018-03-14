package com.teststellar;

import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class AccountUtil {

    public static boolean sendAssetFromAccountToAccount(KeyPair sourcePair, KeyPair destinationAccountPair, int amount, Asset asset, Server server) throws IOException {
        // Start building the transaction.
        Transaction transaction = new Transaction.Builder(server.accounts().account(sourcePair))
                .addOperation(new PaymentOperation.Builder(destinationAccountPair,asset, Integer.toString(amount)).build())
                // A memo allows you to add your own metadata to a transaction. It's
                // optional and does not affect how Stellar treats the transaction.
                .addMemo(Memo.text("Test Transaction!"))
                .build();
        // Sign the transaction to prove you are actually the person sending it.
        transaction.sign(sourcePair);

        // And finally, send it off to Stellar!
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            if(response.isSuccess()) {
                System.out.println("Succesfull transaction of " + amount + "" + asset.getType() + "!");
            }
            else{
                System.out.println("Something went wrong!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
            // If the result is unknown (no response body, timeout etc.) we simply resubmit
            // already built transaction:
            // SubmitTransactionResponse response = server.submitTransaction(transaction);
        }

        return true;
    }

    public static KeyPair createNewAccountOnTest(Integer startingBalance) throws IOException {
        Network.useTestNetwork();
        Server server = new Server("https://horizon-testnet.stellar.org");

        // create a completely new and unique pair of keys.
        // see more about KeyPair objects: https://stellar.github.io/java-stellar-sdk/org/stellar/sdk/KeyPair.html
        KeyPair newAccount = KeyPair.random();
        KeyPair godAccount = KeyPair.fromSecretSeed(AccountStore.GOD_SECRET_KEY);

        //Let's create an account with a starting balance of 42000XLM because 42 is the answer :D
        String starting = startingBalance == null ? "42000" : Integer.toString(startingBalance);
        Transaction transaction = new Transaction.Builder(server.accounts().account(godAccount))
                .addOperation(new CreateAccountOperation.Builder(newAccount, starting).build())
                // A memo allows you to add your own metadata to a transaction. It's
                // optional and does not affect how Stellar treats the transaction.
                .addMemo(Memo.text("Test Transaction!"))
                .build();
        // Sign the transaction to prove you are actually the person sending it.
        transaction.sign(godAccount);

        // And finally, send it off to Stellar!
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            System.out.println("Successfully created new account " + newAccount.getAccountId() + "! Secret seed: " + newAccount.getSecretSeed());
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            System.out.println(e.getMessage());
            // If the result is unknown (no response body, timeout etc.) we simply resubmit
            // already built transaction:
            // SubmitTransactionResponse response = server.submitTransaction(transaction);
        }


        return newAccount;
    }

    public static AccountResponse getTestAccount(@NotNull  KeyPair account) throws Exception {

        Network.useTestNetwork();
        Server server = new Server("https://horizon-testnet.stellar.org");

        AccountResponse accountResponse = null;
        try {
            accountResponse = server.accounts().account(account);
        } catch (IOException e) {
            System.out.print("Problem I/O invalid");
            throw e;
        }

        return accountResponse;
    }

    public static void printAccountBalances(AccountResponse accountResponse) {
        //System.out.println("Balances for account " + accountResponse.getKeypair().getAccountId());
        for (AccountResponse.Balance balance : accountResponse.getBalances()) {
            System.out.println(String.format(
                    "Type: %s, Code: %s, Balance: %s",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));
        }
    }

}
