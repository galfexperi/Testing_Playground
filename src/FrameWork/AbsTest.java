package FrameWork;

import org.junit.runners.Suite;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by navot.dako on 2/24/2016.
 */
public abstract class AbsTest {
    protected int success =0;
    protected int repNum = 0;
    protected MyClient client = null;
    protected String device;
    protected String reportFolder;
    protected String deviceOS;
    protected String testName;

    public AbsTest(MyClient client, String device, int repNum, String reportFolder, String deviceOS, String testName){

        this.client = client;
        this.repNum  = repNum;
        this.reportFolder = reportFolder;
        this.deviceOS = deviceOS;
        this.testName = testName;
        getDevice();

    }

    private void getDevice() {
        device = client.waitForDevice("@os = '"+deviceOS+"'", 10000);
        System.out.println(Thread.currentThread().getName()+ " - "+ device.substring(device.indexOf(":")));
        client.openDevice();
        client.sendText("{HOME}");

        //client.setProjectBaseDirectory(projectBaseDirectory);
        //( contains(@version,'9.') or contains(@version,'4.') )and

        //client.setShowPassImageInReport(false);
        // client.setProperty("on.device.xpath", "true");
    }

    public void runTest(){
        long time =0;
        for (int i = 0; i < repNum; i++) {
            try{
                System.out.println(Thread.currentThread().getName() +"  STARTING - " +device+ " - " +Thread.currentThread().getName()+": Iteration - " + (i+1));
                System.out.println(Thread.currentThread().getName() +"  Set Reporter - " + client.setReporter("xml", reportFolder, device.substring(8) + " "+ deviceOS +" - "+ testName+ " - "+ (i+1) ));
                long before =   System.currentTimeMillis();
                if (deviceOS.equals("ios")){
                    IOSRunTest();
                }
                else{
                    AndroidRunTest();
                }
                time = System.currentTimeMillis() - before;
                success ++;
                String stringToWrite = "SUCCESS - " +device+" - "+testName +" - " +Thread.currentThread().getName()+": Iteration - " + (i+1) + " - Success Rate: "+success+"/"+(i+1)+" = "+(success/(i+1)) + "    Time - "+time/1000 +"s";
                System.out.println(Thread.currentThread().getName() +"  ############################ "+stringToWrite+" ##############################");
                try {
                    Write(stringToWrite);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }catch(Exception e ){
                Failure(i, e,time);
            }try {
                System.out.println(Thread.currentThread().getName() + "  " + device + " - " + "REPORT - " + client.generateReport(false));
            }catch(Exception e){
                e.printStackTrace();
                getDevice();
            }
        }
        System.out.println("############################ Device - "+ device +" - "+testName +" - "+Thread.currentThread().getName() +" - Finished #############################" );
        //client.closeDevice();
        client.releaseClient();

    }

    public void Failure(int i, Exception e, long time) {

        String stringToWrite = "FAILURE - " +device+" - "+testName+ " - " +Thread.currentThread().getName()+": Iteration - " + (i+1) + " - Success Rate: "+success+"/"+(i+1)+" = "+  (double)(success/(i+1)) + "    Time - "+time/1000 +"s";
        System.err.println("****************** ############################ " + stringToWrite + " ############################# ******************");
        System.err.println(device + " - StackTrace: "); System.err.println(device + " - "+e.getMessage()); e.printStackTrace();

        try {
            Write("\n*** "+stringToWrite+" ***");
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Write("         "+device + " - "+errors.toString());
        } catch (IOException e1) {
            System.err.println(device + " - can't write stacktrace to report.txt :");
            e1.printStackTrace();
        }
        try{
            System.err.println(device+" - "+"Log - "+client.getDeviceLog());

        }catch(Exception e1){
            System.err.println(device + " - Can't get Device Log");
        }
        try{
            System.err.println(device+" - "+"SupportData - ");
            String dataPath =reportFolder+"\\SupportData_"+device+"_"+System.currentTimeMillis();
            client.collectSupportData(dataPath,null,null,"","","",true,true);
            client.report(dataPath,true);
        }catch(Exception e1){
            System.err.println(device + " - Can't get SupportData");
            e1.printStackTrace();
        }

    }

    protected abstract void AndroidRunTest();

    protected abstract void IOSRunTest();

    public void Write(String stringToWrite) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("Reports/report.txt", true)));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        writer.append(sdf.format(new Date(System.currentTimeMillis())) +": " + stringToWrite+"\n");
        writer.close();

    }
}