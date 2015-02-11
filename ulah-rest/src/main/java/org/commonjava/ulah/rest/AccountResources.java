package org.commonjava.ulah.rest;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.commonjava.ulah.db.AccountDataManager;
import org.commonjava.ulah.db.AccountTransactionDataManager;
import org.commonjava.ulah.model.Account;
import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.anno.Routes;
import org.commonjava.vertx.vabr.helper.RequestHandler;
import org.commonjava.vertx.vabr.types.ApplicationStatus;
import org.commonjava.vertx.vabr.types.Method;
import org.commonjava.vertx.vabr.util.Respond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

@Handles("/accounts")
public class AccountResources implements RequestHandler {

    public static final String BY_ID_PATH_PART = "by-id";

    public static final String ACCOUNT_ID_PARAM = "id";

    public static final String NAME_PARAM = "name";

    @Inject
    private AccountDataManager accounts;

    @Inject
    private AccountTransactionDataManager transactions;

    @Inject
    private ObjectMapper mapper;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected AccountResources() {
    }

    public AccountResources(AccountDataManager accounts,
            AccountTransactionDataManager transactions, ObjectMapper mapper) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.mapper = mapper;
    }

    @Routes({ @Route(path = "/by-id/:id/balance", method = Method.GET) })
    public void balance(HttpServerRequest req) {
        req.endHandler(v -> {
            String idStr = req.params().get(ACCOUNT_ID_PARAM);
            Integer acctId = Integer.valueOf(idStr);

            transactions
            .getAccountBalance(
                    acctId,
                    balance -> {
                        // TODO: Is this right??
                        balance.setScale(2, RoundingMode.HALF_UP);

                        Map<String, Object> dto = new HashMap<>();
                        dto.put("account", idStr);
                        dto.put("balance", balance);

                        try {
                            Respond.to(req).jsonEntity(balance, mapper)
                            .ok().send();
                        } catch (Exception e) {
                            logger.error(
                                    String.format(
                                            "Failed to retrieve account balance for: %s. Reason: %s",
                                            idStr, e.getMessage()), e);

                            Respond.to(req).serverError(e, true);
                        }
                    });
        });
    }

    @Routes({ @Route(path = "/", method = Method.GET, routeKey = "rootGet"),
        @Route(path = "/all", method = Method.GET, routeKey = "allGet") })
    public void list(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            accounts.listAccounts(accts -> {
                try {
                    Respond.to(req)
                    .jsonEntity(
                            Collections.singletonMap("items", accts),
                            mapper).ok().send();
                } catch (Exception e) {
                    logger.error(String.format(
                            "Failed to render account listing: %s",
                            e.getMessage()), e);

                    Respond.to(req).serverError(e, true);
                }
            });
        });
    }

    @Routes({ @Route(path = "/by-id/:id", method = Method.GET) })
    public void getById(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String idStr = req.params().get(ACCOUNT_ID_PARAM);
            Integer acctId = Integer.valueOf(idStr);
            accounts.getAccount(acctId, acct -> {
                if (acct == null) {
                    Respond.to(req).status(ApplicationStatus.NOT_FOUND).send();
                } else {
                    try {
                        Respond.to(req).jsonEntity(acct, mapper).ok().send();
                    } catch (Exception e) {
                        logger.error(String.format(
                                "Failed to retrieve account: %s. Reason: %s",
                                idStr, e.getMessage()), e);

                        Respond.to(req).serverError(e, true);
                    }
                }
            });
        });
    }

    @Routes({ @Route(path = "/by-name/:name", method = Method.GET) })
    public void getByName(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String name = req.params().get(NAME_PARAM);
            accounts.getAccount(name, acct -> {
                if (acct == null) {
                    Respond.to(req).status(ApplicationStatus.NOT_FOUND).send();
                } else {
                    try {
                        Respond.to(req).jsonEntity(acct, mapper).ok().send();
                    } catch (Exception e) {
                        logger.error(String.format(
                                "Failed to retrieve account: %s. Reason: %s",
                                name, e.getMessage()), e);

                        Respond.to(req).serverError(e, true);
                    }
                }
            });
        });
    }

    @Routes({ @Route(path = "/by-id/:id", method = Method.DELETE) })
    public void deleteById(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String idStr = req.params().get(ACCOUNT_ID_PARAM);
            Integer acctId = Integer.valueOf(idStr);
            accounts.getAccount(acctId,
                    acct -> {
                        if (acct == null) {
                            Respond.to(req).status(ApplicationStatus.NOT_FOUND)
                            .send();
                        } else {
                            accounts.deleteAccount(acct);
                            Respond.to(req)
                            .status(ApplicationStatus.NO_CONTENT)
                            .send();
                        }
                    });
        });
    }

    @Routes({ @Route(path = "/by-name/:name", method = Method.DELETE) })
    public void deleteByName(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String name = req.params().get(NAME_PARAM);
            accounts.getAccount(name,
                    acct -> {
                        if (acct == null) {
                            Respond.to(req).status(ApplicationStatus.NOT_FOUND)
                            .send();
                        } else {
                            accounts.deleteAccount(acct);
                            Respond.to(req)
                            .status(ApplicationStatus.NO_CONTENT)
                            .send();
                        }
                    });
        });
    }

    @Routes({
            @Route(path = "/", method = Method.POST, routeKey = "create"),
            @Route(path = "/by-id/:id", method = Method.PUT, routeKey = "store") })
    public void createOrStore(HttpServerRequest req, Buffer body) {
        req.endHandler(v -> {
            String json = body.getString(0, body.length());
            try {
                Account account = mapper.readValue(json, Account.class);
                accounts.storeAccount(account, acct -> {
                    String uri = req.uri();
                    try {
                        Respond r = Respond.to(req).jsonEntity(acct, mapper);

                        Method m = Method.valueOf(req.method());
                        if (m == Method.PUT) {
                            r.created(uri, BY_ID_PATH_PART,
                                    Integer.toString(acct.getId()));
                        } else {
                            r.ok();
                        }

                        r.send();
                    } catch (Exception e) {
                        logger.error(
                                String.format("Failed to store account: %s",
                                        e.getMessage()), e);

                        Respond.to(req).serverError(e, true);
                    }
                });
            } catch (Exception e) {
                logger.error(
                        String.format("Failed to store account: %s",
                                e.getMessage()), e);

                Respond.to(req).serverError(e, true);
            }
        });
    }

}
