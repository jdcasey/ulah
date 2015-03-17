package org.commonjava.ulah.rest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.commonjava.ulah.db.AccountDataManager;
import org.commonjava.ulah.db.AccountTransactionDataManager;
import org.commonjava.ulah.dto.AccountTransactionDTO;
import org.commonjava.ulah.dto.TransactionListDTO;
import org.commonjava.ulah.model.AccountTransaction;

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResources implements RestResources {

    public static final String ID_PARAM = "id";

    public static final String FROM_DATE_QUERY_PARAM = "q_from";

    public static final String TO_DATE_QUERY_PARAM = "q_to";

    public static final String SKIP_QUERY_PARAM = "q_skip";

    public static final String LIMIT_QUERY_PARAM = "q_limit";

    public static final Integer DEFAULT_LIMIT = 25;

    public static final String TXN_PATH_PART = "txn";

    @Inject
    private AccountTransactionDataManager transactions;

    @Inject
    private AccountDataManager accounts;

    protected TransactionResources() {
    }

    public TransactionResources(AccountTransactionDataManager transactions,
            AccountDataManager accounts) {
        this.transactions = transactions;
        this.accounts = accounts;
    }

    @Path("/txn/{id}")
    @DELETE
    public Response delete(@PathParam("id") String txnId) {
        Long id = Long.valueOf(txnId);
        AccountTransaction txn = transactions.getTransaction(id);

        if (txn == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            transactions.deleteTransaction(txn);
            return Response.noContent().build();
        }
    }

    @POST
    public AccountTransaction create(AccountTransactionDTO transaction) {
        return createOrStore(() -> transaction);
    }

    @Path("/txn/{id}")
    @PUT
    public AccountTransaction store(AccountTransactionDTO transaction,
            @PathParam("id") String txnId) {
        return createOrStore(() -> {
            transaction.setId(Long.valueOf(txnId));
            return transaction;
        });
    }

    private AccountTransaction createOrStore(
            Supplier<AccountTransactionDTO> txnSupplier) {
        AccountTransactionDTO dto = txnSupplier.get();
        AccountTransaction txn = dto.toTransaction(accounts);

        return transactions.storeTransaction(txn);
    }

    @Path("/all")
    @GET
    public TransactionListDTO getAllTransactions(
            @QueryParam(TransactionResources.FROM_DATE_QUERY_PARAM) String fromStr,
            @QueryParam(TransactionResources.TO_DATE_QUERY_PARAM) String toStr,
            @QueryParam(TransactionResources.SKIP_QUERY_PARAM) String skipStr,
            @QueryParam(TransactionResources.LIMIT_QUERY_PARAM) String limitStr) {

        Date from = null;
        if (fromStr != null) {
            from = Date.from(LocalDateTime.parse(fromStr)
                    .atZone(ZoneId.systemDefault()).toInstant());
        }

        Date to = null;
        if (toStr != null) {
            to = Date.from(LocalDateTime.parse(toStr)
                    .atZone(ZoneId.systemDefault()).toInstant());
        }

        Integer skip = 0;
        if (skipStr != null) {
            skip = Integer.valueOf(skipStr);
        }

        Integer limit = DEFAULT_LIMIT;
        if (limitStr != null) {
            limit = Integer.valueOf(limitStr);
        }

        List<AccountTransaction> txns = transactions.getAllTransactions(from,
                to, skip, limit);

        List<AccountTransactionDTO> dtos = txns.stream()
                .map(txn -> new AccountTransactionDTO(txn))
                .collect(Collectors.toList());

        return new TransactionListDTO(dtos);
    }

    @Path("/in/{id}")
    @GET
    public TransactionListDTO getAccountTransactions(
            @PathParam("id") String acctStr,
            @QueryParam(TransactionResources.FROM_DATE_QUERY_PARAM) String fromStr,
            @QueryParam(TransactionResources.TO_DATE_QUERY_PARAM) String toStr,
            @QueryParam(TransactionResources.SKIP_QUERY_PARAM) String skipStr,
            @QueryParam(TransactionResources.LIMIT_QUERY_PARAM) String limitStr) {
        Integer acctId = Integer.valueOf(acctStr);

        Date from = null;
        if (fromStr != null) {
            from = Date.from(LocalDateTime.parse(fromStr)
                    .atZone(ZoneId.systemDefault()).toInstant());
        }

        Date to = null;
        if (toStr != null) {
            to = Date.from(LocalDateTime.parse(toStr)
                    .atZone(ZoneId.systemDefault()).toInstant());
        }

        Integer skip = 0;
        if (skipStr != null) {
            skip = Integer.valueOf(skipStr);
        }

        Integer limit = DEFAULT_LIMIT;
        if (limitStr != null) {
            limit = Integer.valueOf(limitStr);
        }

        List<AccountTransaction> txns = transactions.getAccountTransactions(
                acctId, from, to, skip, limit);

        List<AccountTransactionDTO> dtos = txns.stream()
                .map(txn -> new AccountTransactionDTO(txn))
                .collect(Collectors.toList());

        return new TransactionListDTO(dtos);
    }

}
