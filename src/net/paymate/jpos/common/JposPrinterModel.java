package net.paymate.jpos.common;

import net.paymate.awtx.print.PrinterModel;

/**  wraps jpos printer with paymate state control
 * Created by: andyh
 * Date: Mar 1, 2005   9:59:36 PM
 * (C) 2005 hal42
 */
public class JposPrinterModel extends PrinterModel{
    jpos.POSPrinter jlpt;
    public JposPrinterModel(jpos.POSPrinter jlpt) {
        super();
        this.jlpt=jlpt;
    }

}
