package database;

import Other.OmgevingConfig;
import Testutil.LogStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tjitte.bouma on 14-2-2017.
 */
public class SqlPlusClient implements Runnable {
    private String sqlBuffer;
    private String scriptName;
    private boolean running;
    private String dbString;
    private CountDownLatch currentCdl;

    private Process sqlPlusProcess;

    private LogStreamReader stdOutReader;
    private LogStreamReader stdErrorReader;
    // Een AtomicBoolean die aangeeft of er errors zijn opgetreden tijdens het uitvoeren van een betreffende sql-script.
    private final AtomicBoolean hasErrors;

    public SqlPlusClient(String dbString, AtomicBoolean hasErrors) {
        Logger log = LoggerFactory.getLogger(SqlPlusClient.class);
        sqlBuffer = null;
        running = true;
        this.dbString = dbString;
        this.hasErrors = hasErrors;
        String command = "sqlplus " + dbString; // Commandline uitvoering, met sqlplus en de inlog en database gegevens.
        String omgeving = OmgevingConfig.getOmgeving();
        try {
            System.out.println("Executing command: " + command);
            sqlPlusProcess = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException("Could not execute " + command, e);
        }

        stdOutReader = new LogStreamReader(sqlPlusProcess.getInputStream(), log, false, hasErrors);
        Thread stdOutThread = new Thread(stdOutReader, "StdOutLog");
        stdOutThread.start();

        stdErrorReader = new LogStreamReader(sqlPlusProcess.getErrorStream(), log, true, hasErrors);
        Thread errThread = new Thread(stdErrorReader, "ErrLog");
        errThread.start();
    }

    @Override
    public void run() {
        OutputStream stdin = sqlPlusProcess.getOutputStream();

        PrintWriter dPrintWriter = new PrintWriter(stdin);

        while (running) {
            while (running && sqlBuffer == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread interrupted.", e);
                }
            }
            if (!running) {
                continue;
            }
            // Voor het uitvoeren van het vorige script nog controleren of er toen fouten zijn opgetreden.
            if (hasErrors()) {
                stop();
                break;
            }

            stdOutReader.clearAll();
            String terminatorString = "END OF SCRIPT MARKER [" + System.currentTimeMillis() + "]";

            sqlBuffer = sqlBuffer + "\n\nprompt " + terminatorString;
            System.out.println("Starting sql stream for '" + scriptName + "' in database '" + dbString + "'.");
            try {
                for (String line : sqlBuffer.split("\r\n")) {
                    dPrintWriter.println(line);
                    dPrintWriter.flush();
                    stdin.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not flush stdin.", e);
            }
            scriptName = null;
            sqlBuffer = null;

            while (stdOutReader.getAll().indexOf(terminatorString) == -1)  {
                if (hasErrors ()) {
                    // Na het uitvoeren van het script zijn er fouten opgetreden.
                    // Het script stopt zodra er fouten optreden, hier dus ook stoppen!
                    stop();
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Could not sleep ", e);
                    }
                }
            }

            currentCdl.countDown();
        }

        // send exit command
        dPrintWriter.println("exit");
        dPrintWriter.flush();
        try {
            stdin.flush();
            sqlPlusProcess.waitFor();
            dPrintWriter.close();
            stdin.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not flush stdin.", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not waitFor.", e);
        }
    }

    public void stop() {
        running = false;
    }

    public boolean hasErrors () {
        return stdErrorReader.getHasErrors().get() || stdOutReader.getHasErrors().get();
    }

    public String getName () {
        return dbString;
    }

    public void fillBuffer(String scriptName, String script, CountDownLatch cdl) {
        if (this.scriptName != null || this.sqlBuffer != null) {
            throw new RuntimeException("SqlBuffer wordt gevuld terwijl de vorige run nog niet voltooid is.");
        }
        this.scriptName = scriptName;
        this.sqlBuffer = script;
        this.currentCdl = cdl;
        this.stdOutReader.setLogPrefix(scriptName + " - " + dbString + " - ");
        this.stdErrorReader.setLogPrefix(scriptName + " - " + dbString + " - ");
    }

    public AtomicBoolean getHasErrors() {
        return hasErrors;
    }

    public String getTotNuToeUitgevoerdeSqlCode () {
        return stdOutReader.getAll().toString() + " - " + stdErrorReader.getAll().toString();
    }
}
