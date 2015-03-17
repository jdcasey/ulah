package org.commonjava.ulah.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.commonjava.ulah.db.AccountDataManager;
import org.commonjava.ulah.db.AccountTransactionDataManager;
import org.commonjava.ulah.dto.AccountBalanceDTO;
import org.commonjava.ulah.dto.AccountListDTO;
import org.commonjava.ulah.model.Account;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResources implements RestResources {

    public static final String BY_ID_PATH_PART = "by-id";

    public static final String ACCOUNT_ID_PARAM = "id";

    public static final String NAME_PARAM = "name";

    @Inject
    private AccountDataManager accounts;

    @Inject
    private AccountTransactionDataManager transactions;

    protected AccountResources() {
    }

    public AccountResources(AccountDataManager accounts,
            AccountTransactionDataManager transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Path("/by-id/{id}/balance")
    @GET
    public AccountBalanceDTO balance(@PathParam("id") String idStr) {
        Integer acctId = Integer.valueOf(idStr);
        BigDecimal balance = transactions.getAccountBalance(acctId);
        if (balance != null) {
            balance.setScale(2, RoundingMode.HALF_UP);
        }

        return new AccountBalanceDTO(acctId, balance);
    }

    @GET
    public AccountListDTO rootList() {
        return list();
    }

    @Path("/all")
    @GET
    public AccountListDTO allList() {
        return list();
    }

    private AccountListDTO list() {
        List<Account> accountList = accounts.listAccounts();
        return new AccountListDTO(accountList);
    }

    @Path("/by-id/{id}")
    @GET
    public Account getById(@PathParam("id") String idStr) {
        Integer acctId = Integer.valueOf(idStr);
        Account acct = accounts.getAccount(acctId);
        return acct;
    }

    @Path("/by-name/{name}")
    @GET
    public Account getByName(@PathParam("name") String name) {
        return accounts.getAccount(name);
    }

    @Path("/by-id/{id}")
    @DELETE
    public Response deleteById(@PathParam("id") String idStr) {
        return delete(() -> {
            Integer acctId = Integer.valueOf(idStr);
            return accounts.getAccount(acctId);
        });
    }

    private Response delete(Supplier<Account> accountSupplier) {
        Account acct = accountSupplier.get();
        if (acct == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            accounts.deleteAccount(acct);
            return Response.noContent().build();
        }
    }

    @Path("/by-name/{name}")
    @DELETE
    public Response deleteByName(@PathParam("name") String name) {
        return delete(() -> {
            return accounts.getAccount(name);
        });
    }

    @POST
    public Account createAccount(Account account) {
        return createOrStore(() -> account);
    }

    @Path("/by-id/{id}")
    @PUT
    public Account storeAccount(Account account, @PathParam("id") String idStr) {
        return createOrStore(() -> {
            account.setId(Integer.valueOf(idStr));

            return account;
        });
    }

    private Account createOrStore(Supplier<Account> accountSupplier) {
        Account account = accountSupplier.get();
        account = accounts.storeAccount(account);

        return account;
    }

}
