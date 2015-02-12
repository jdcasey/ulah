package org.commonjava.ulah.route;

import org.commonjava.vertx.vabr.anno.Handles;
import org.commonjava.vertx.vabr.helper.RequestHandler;

@Handles(prefix = "/", key = "ui")
@UIApp
public class UIHandler implements RequestHandler {

}
