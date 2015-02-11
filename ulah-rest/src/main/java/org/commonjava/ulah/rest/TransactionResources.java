package org.commonjava.ulah.rest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.commonjava.ulah.db.AccountDataManager;
import org.commonjava.ulah.db.AccountTransactionDataManager;
import org.commonjava.ulah.dto.AccountTransactionDTO;
import org.commonjava.ulah.model.AccountTransaction;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.anno.Routes;
import org.commonjava.vertx.vabr.types.ApplicationStatus;
import org.commonjava.vertx.vabr.types.Method;
import org.commonjava.vertx.vabr.util.Respond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

@Handles("/transactions")
public class TransactionResources {

    public static final String ID_PARAM = "id";

    public static final String FROM_DATE_QUERY_PARAM = "q_from";

    public static final String TO_DATE_QUERY_PARAM = "q_to";

    public static final String SKIP_QUERY_PARAM = "q_skip";

    public static final String LIMIT_QUERY_PARAM = "q_limit";

    public static final Integer DEFAULT_LIMIT = 25;

    public static final String TXN_PATH_PART = "txn";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AccountTransactionDataManager transactions;

    @Inject
    private AccountDataManager accounts;

    @Inject
    private ObjectMapper mapper;

    protected TransactionResources() {
    }

    public TransactionResources(AccountTransactionDataManager transactions,
            AccountDataManager accounts, ObjectMapper mapper) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.mapper = mapper;
    }

    @Routes({ @Route(path = "/txn/:id", method = Method.DELETE) })
    public void delete(HttpServerRequest req) {
        req.endHandler(v -> {
            String txnId = req.params().get(ID_PARAM);
            Long id = Long.valueOf(txnId);
            AccountTransaction txn = transactions.getTransaction(id);

            if (txn == null) {
                Respond.to(req).status(ApplicationStatus.NOT_FOUND).send();
            } else {
                transactions.deleteTransaction(txn);
                Respond.to(req).status(ApplicationStatus.NO_CONTENT).send();
            }
        });
    }

    @Routes({ @Route(path = "/", method = Method.POST, routeKey = "create"),
        @Route(path = "/txn/:id", method = Method.PUT, routeKey = "store") })
    public void createOrStore(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String json = body.getString(0, body.length());
            try {
                AccountTransactionDTO dto = mapper.readValue(json,
                        AccountTransactionDTO.class);

                AccountTransaction txn = dto.toTransaction(accounts);

                transactions.storeTransaction(txn, t -> {
                    String uri = req.uri();
                    try {
                        Respond r = Respond.to(req).jsonEntity(t, mapper);

                        Method m = Method.valueOf(req.method());
                        if (m == Method.PUT) {
                            r.created(uri, TXN_PATH_PART,
                                    Long.toString(t.getId()));
                        } else {
                            r.ok();
                        }

                        r.send();
                    } catch (Exception e) {
                        logger.error(String.format(
                                "Failed to store transaction: %s",
                                e.getMessage()), e);

                        Respond.to(req).serverError(e, true);
                    }
                });
            } catch (Exception e) {
                logger.error(
                        String.format("Failed to store transaction: %s",
                                e.getMessage()), e);

                Respond.to(req).serverError(e, true);
            }
        });
    }

    @Routes({ @Route(path = "/all", method = Method.GET) })
    public void getAllTransactions(HttpServerRequest req) {
        req.endHandler(v -> {
            Date from = null;
            String fromStr = req.params().get(FROM_DATE_QUERY_PARAM);
            if (fromStr != null) {
                from = Date.from(LocalDateTime.parse(fromStr)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }

            Date to = null;
            String toStr = req.params().get(TO_DATE_QUERY_PARAM);
            if (toStr != null) {
                to = Date.from(LocalDateTime.parse(toStr)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }

            Integer skip = 0;
            String skipStr = req.params().get(SKIP_QUERY_PARAM);
            if (skipStr != null) {
                skip = Integer.valueOf(skipStr);
            }

            Integer limit = DEFAULT_LIMIT;
            String limitStr = req.params().get(LIMIT_QUERY_PARAM);
            if (limitStr != null) {
                limit = Integer.valueOf(limitStr);
            }

            transactions
            .getAllTransactions(
                    from,
                    to,
                    skip,
                    limit,
                    txns -> {
                        List<AccountTransactionDTO> dtos = txns
                                .stream()
                                .map(txn -> new AccountTransactionDTO(
                                        txn))
                                        .collect(Collectors.toList());

                        try {
                            Respond.to(req)
                            .jsonEntity(
                                    Collections.singletonMap(
                                            "items", dtos),
                                            mapper).ok().send();
                        } catch (Exception e) {
                            logger.error(
                                    String.format(
                                            "Failed to retrieve transactions: %s\nFrom: %s\nTo: %s\nSkip: %s\nLimit: %s\n\n",
                                            e.getMessage(), fromStr,
                                            toStr, skipStr, limitStr),
                                            e);
                            Respond.to(req).serverError(e, true);
                        }
                    });
        });
    }

    @Routes({ @Route(path = "/in/:id", method = Method.GET) })
    public void getAccountTransactions(HttpServerRequest req) {
        req.endHandler(v -> {
            String acctStr = req.params().get(ID_PARAM);
            Integer acctId = Integer.valueOf(acctStr);

            Date from = null;
            String fromStr = req.params().get(FROM_DATE_QUERY_PARAM);
            if (fromStr != null) {
                from = Date.from(LocalDateTime.parse(fromStr)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }

            Date to = null;
            String toStr = req.params().get(TO_DATE_QUERY_PARAM);
            if (toStr != null) {
                to = Date.from(LocalDateTime.parse(toStr)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }

            Integer skip = 0;
            String skipStr = req.params().get(SKIP_QUERY_PARAM);
            if (skipStr != null) {
                skip = Integer.valueOf(skipStr);
            }

            Integer limit = DEFAULT_LIMIT;
            String limitStr = req.params().get(LIMIT_QUERY_PARAM);
            if (limitStr != null) {
                limit = Integer.valueOf(limitStr);
            }

            transactions
            .getAccountTransactions(
                    acctId,
                    from,
                    to,
                    skip,
                    limit,
                    txns -> {
                        List<AccountTransactionDTO> dtos = txns
                                .stream()
                                .map(txn -> new AccountTransactionDTO(
                                        txn))
                                        .collect(Collectors.toList());

                        try {
                            Respond.to(req)
                            .jsonEntity(
                                    Collections.singletonMap(
                                            "items", dtos),
                                            mapper).ok().send();
                        } catch (Exception e) {
                            logger.error(
                                    String.format(
                                            "Failed to retrieve account transactions: %s\nAccount: %s\nFrom: %s\nTo: %s\nSkip: %s\nLimit: %s\n\n",
                                            e.getMessage(), acctStr,
                                            fromStr, toStr, skipStr,
                                            limitStr), e);
                            Respond.to(req).serverError(e, true);
                        }
                    });
        });
    }

}
