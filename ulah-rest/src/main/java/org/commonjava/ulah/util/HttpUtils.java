/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.ulah.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class HttpUtils {

    private HttpUtils() {
    }

    private static final String DATE_HEADER_FMT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public static String formatDateHeader(final long date) {
        return new SimpleDateFormat(DATE_HEADER_FMT).format(new Date(date));
    }

    public static String formatDateHeader(final Date date) {
        return new SimpleDateFormat(DATE_HEADER_FMT).format(date);
    }

    public static Date parseDateHeader(final String date) throws ParseException {
        return new SimpleDateFormat(DATE_HEADER_FMT).parse(date);
    }

}
