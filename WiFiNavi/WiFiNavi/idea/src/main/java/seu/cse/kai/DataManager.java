package seu.cse.kai;

/**
 * Created by Dell on 2015/7/22.
 */

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;


public class DataManager {

    public int NUM_OF_APS = 14;
    public int NUM_OF_POIS = 60;
    public int NUM_OF_RECS = 6600; // 6591
    String [] MAC = new String [NUM_OF_APS];
    String [] POS = new String [NUM_OF_POIS];
    String [][] RSSI = new String [NUM_OF_RECS][NUM_OF_APS];
    int countR = -1; // index of POIs
    int countC = 0; // index of APs

    public DataManager() {

    }

    public void filePreprocess() throws Exception {
        String urlImport = ".\\data\\import.txt";
        FileInputStream fi;
        fi = new FileInputStream(urlImport);
        BufferedReader br = new BufferedReader(new InputStreamReader(fi));
        String str = "";

		/*
		 * e.g.
		# timestamp=2006-02-11 22:14:37# usec=250# minReadings=110
		t=1139692477303;id=00:02:2D:21:0F:33;pos=0.0,0.05,0.0;degree=130.5;00:14:bf:b1:97:8a=-43,2437000000,3;00:0f:a3:39:e1:c0=-52,2462000000,3;00:14:bf:3b:c7:c6=-62,2432000000,3;00:14:bf:b1:97:81=-58,2422000000,3;00:14:bf:b1:97:8d=-62,2442000000,3;00:14:bf:b1:97:90=-57,2427000000,3;00:0f:a3:39:e0:4b=-79,2462000000,3;00:0f:a3:39:e2:10=-88,2437000000,3;00:0f:a3:39:dd:cd=-64,2412000000,3;02:64:fb:68:52:e6=-87,2447000000,1;02:00:42:55:31:00=-85,2457000000,1
		 */
        while(br.ready()) {
            str = br.readLine();
            if (str.startsWith("#")) {
                // # timestamp=2006-02-11 22:14:37# usec=250# minReadings=110
                continue;
            }
            else {
                countR ++;
                String strPos = "";
                str = str.substring(41);
                // t=1139692477303;id=00:02:2D:21:0F:33;pos=
                char c;
                do {
                    c = str.charAt(0);
                    strPos += c;
                    str = str.substring(1);
                }
                while (c != ';');
                // strPos = strPos.substring(0,strPos.length()-1);
                // 0.0,0.05,0.0;

                if (str.length() < 30)
                    continue;

                do {
                    c = str.charAt(0);
                    str = str.substring(1);
                }
                while (c != ';');
                // degree=130.5;

                // 00:14:bf:b1:97:8a=-43,2437000000,3;00:0f:a3:39:e1:c0=-52,2462000000,3;00:14:bf:3b:c7:c6=-62,2432000000,3;00:14:bf:b1:97:81=-58,2422000000,3;00:14:bf:b1:97:8d=-62,2442000000,3;00:14:bf:b1:97:90=-57,2427000000,3;00:0f:a3:39:e0:4b=-79,2462000000,3;00:0f:a3:39:e2:10=-88,2437000000,3;00:0f:a3:39:dd:cd=-64,2412000000,3;02:64:fb:68:52:e6=-87,2447000000,1;02:00:42:55:31:00=-85,2457000000,1
                while (true) {
                    String id = "";
                    while (true) {
                        c = str.charAt(0);
                        if (c != '=') {
                            // 00:14:bf:b1:97:8a
                            id += c;
                            str = str.substring(1);
                            continue;
                        }
                        // =
                        str = str.substring(1);
                        break;
                    }
                    String db = "";
                    while (true) {
                        c = str.charAt(0);
                        if (c != ',') {
                            // -43
                            db += c;
                            str = str.substring(1);
                            continue;
                        }
                        break;
                    }
                    if (str.length() < 14)
                        break;
                    else
                        str = str.substring(14);
                    // ,2437000000,3;

                    int apIndex;
                    for (apIndex = 0; apIndex < this.NUM_OF_APS; apIndex ++) {
                        if (this.MAC[apIndex] == null) {
                            this.MAC[apIndex] = id;
                            countC ++;
                            break;
                        }
                        if (id.equals(this.MAC[apIndex])) {
                            break;
                        }
                    }
                    this.RSSI[countR][apIndex] = db;
                }

                // write to "strPos.txt"
                String urlExport = ".\\data\\export\\" + strPos + ".txt";
                FileOutputStream fo = new FileOutputStream(urlExport, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo));
                for (int i = 0; i < this.NUM_OF_APS; i ++) {
                    bw.append(this.RSSI[countR][i] + "\t");
                }
                bw.append("\r\n");
                bw.close();
                fo.close();
            }
            System.out.println("RECs: " + countR + "; APs: " + countC + ".");
        }
    }

    public String sampling(int num) throws IOException {
        if (num <= 0 || num > this.NUM_OF_POIS)
            return null;

        String out = ".\\data\\sampling\\" + num + "@" + System.currentTimeMillis() + ".txt";
        FileOutputStream fo = new FileOutputStream(out, false); // rewrite
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fo));
        fo = new FileOutputStream(out, true); // append
        bw = new BufferedWriter(new OutputStreamWriter(fo));
        File root = new File(".\\data\\export\\");
        File[] files = root.listFiles();
        Random random = new Random();
        int max = files.length;
        for (int i = 0; i < num; i ++) {

            int r = (int) (max * random.nextDouble()); // random poi

            String pos = files[r].getName();
            pos = pos.substring(0, pos.length()-4); // 0.15,9.42,0.0;.txt
            try {
                int line;
                while (true) {
                    line = (int) (110 * random.nextDouble());

                    BufferedReader br = new BufferedReader(new FileReader(files[r].getPath()));
                    int countline = 0;
                    while (countline < line) {
                        br.readLine();
                        countline ++;
                    }
                    String content = br.readLine();
                    if (content != null) {
                        bw.append(content + "\t" + pos + "\r\n");
                        break;
                    }
                }
                System.out.println(i+":"+pos+"@line"+line);
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            max --;
            if (r != max) {
                files[r] = files[max];
            }
        }
        bw.close();
        fo.close();
        System.out.println("--------------------");
        System.out.println("sampling done");
        return out;
    }

    public void mle(String _filename, int num, boolean flag) throws Exception {

        int [][] RSSI = new int [num][this.NUM_OF_APS];
        double [][] mp_pos = new double [num][2];
        double [][] estimated_mp_pos = new double [num][2];
        double [][] estimated_ap_pos = new double [this.NUM_OF_APS][2];
        double [] RSSI0 = new double [this.NUM_OF_APS];
        double [][] estimated_RSSI = new double [num][this.NUM_OF_APS];

		/*
		 * initialize
		 */
        for (int i = 0; i< num; i ++) {
            for (int j = 0; j < 2; j ++) {
                estimated_mp_pos[i][j] = 1.0;
            }
        }

        for (int i = 0; i < this.NUM_OF_APS; i ++) {
            for (int j = 0; j < 2; j ++) {
                estimated_ap_pos[i][j] = 0.0;
            }
        }
        for (int i = 0; i < this.NUM_OF_APS; i ++) {
            RSSI0[i] = -40.0;
        }
        for (int i = 0; i < num; i ++) {
            for (int j = 0; j < this.NUM_OF_APS; j ++) {
                estimated_RSSI[i][j] = -80.0;
            }
        }

        String urlImport = _filename;
        FileInputStream fi;
        fi = new FileInputStream(urlImport);
        BufferedReader br = new BufferedReader(new InputStreamReader(fi));
        String str = "";
        // -60	-52	null	null	null	null	null	null	-72	null	null	null	null	null		21.45,6.62,0.0;
        int mp_index = 0;
        while (br.ready()) {
            str = br.readLine();
            for (int i = 0; i < this.NUM_OF_APS; i ++) {
                String substr = "";
                char c = str.charAt(0);
                while (c != '\t') {
                    substr += c;
                    str = str.substring(1);
                    c = str.charAt(0);
                }
                str = str.substring(1);
                if (substr.equals("null")) {
                    RSSI[mp_index][i] = -100;
                }
                else {
                    RSSI[mp_index][i] = Integer.parseInt(substr);
                }
            }
            for (int i = 0; i < 2; i ++) {
                String substr = "";
                char c = str.charAt(0);
                while (c != ',') {
                    substr += c;
                    str = str.substring(1);
                    c = str.charAt(0);
                }
                str = str.substring(1);
                mp_pos[mp_index][i] = Double.parseDouble(substr);
            }
            mp_index ++;
        }
        System.out.println("initialization done");

		/*
		// print RSSI
		System.out.println("--------------------");
		System.out.println("RSSI:");
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < this.NUM_OF_APS; j ++) {
				System.out.print(RSSI[i][j]+"\t");
			}
			System.out.println();
		}
		// print mp_pos
		System.out.println("--------------------");
		System.out.println("mp_pos:");
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < 2; j ++) {
				System.out.print(mp_pos[i][j]+"\t");
			}
			System.out.println();
		}
		*/

        if (flag) {
            for (int i = 0; i< num; i ++) {
                for (int j = 0; j < 2; j ++) {
                    estimated_mp_pos[i][j] = mp_pos[i][j];
                }
            }
        }

        double a = 0.00001;
        double n = 4.0;
        double old1_sum_e2 = 0.0;
        double old2_sum_e2 = 0.0;
        for (int loop = 0; loop < 20000; loop ++) {

            double sum_e2 = 0.0;
            for (int i = 0; i< num; i ++) {
                for (int j = 0; j < this.NUM_OF_APS; j ++) {
                    if (RSSI[i][j] == -100)
                        continue;
                    double norm = Math.sqrt(
                            Math.pow(estimated_mp_pos[i][0]-estimated_ap_pos[j][0], 2.0) +
                                    Math.pow(estimated_mp_pos[i][1]-estimated_ap_pos[j][1], 2.0));
                    double e = RSSI[i][j] - RSSI0[j] + 10 * n * Math.log10(norm);
                    double e2 = Math.pow(e, 2.0);
                    sum_e2 += e2;
                    // System.out.println(norm + " " + e + " " + e2 + " "+ sum_e2);

                    // stochastic gradient descent
                    double mx = estimated_mp_pos[i][0];
                    double my = estimated_mp_pos[i][1];
                    double ax = estimated_ap_pos[j][0];
                    double ay = estimated_ap_pos[j][1];

                    switch (loop - loop / 5 * 5) {
                        case 0:
                            // estimated_mp_pos[i][0] = estimated_mp_pos[i][0] - a * (2.0 * e * 10.0 * n / Math.log(10.0) / norm * (mx - ax));
                            estimated_mp_pos[i][0] = mx - a * e * (mx - ax);
                        case 1:
                            // estimated_mp_pos[i][1] = estimated_mp_pos[i][1] - a * (2.0 * e * 10.0 * n / Math.log(10.0) / norm * (my - ay));
                            estimated_mp_pos[i][1] = my - a * e * (my - ay);
                        case 2:
                            // estimated_ap_pos[j][0] = estimated_ap_pos[j][0] - a * (2.0 * e * 10.0 * n / Math.log(10.0) / norm * (ax - mx));
                            estimated_ap_pos[j][0] = ax - a * e * (ax - mx);
                        case 3:
                            // estimated_ap_pos[j][1] = estimated_ap_pos[j][1] - a * (2.0 * e * 10.0 * n / Math.log(10.0) / norm * (ay - my));
                            estimated_ap_pos[j][1] = ay - a * e * (ay - my);
                        case 4:
                            RSSI0[j] =  RSSI0[j] + a * e / 20.0 / 4.0 * Math.log(10.0) * norm;
                    }
                    // RSSI0[j] = RSSI0[j] + a * (2.0 * e);
                }
            }

            System.out.println("Loop " + loop + ": sum_error = " + sum_e2);
            if (sum_e2 == old1_sum_e2 && sum_e2 == old2_sum_e2)
                break;
            old1_sum_e2 = sum_e2;
            old2_sum_e2 = old1_sum_e2;
        }

        MyFrame frame = new MyFrame(estimated_mp_pos, mp_pos, num);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        MyFrame frame0 = new MyFrame(mp_pos, mp_pos, num);
        frame0.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.display();
        frame0.display();

        for (int i = 0; i < this.NUM_OF_APS; i ++) {
            System.out.println(RSSI0[i]);
        }
    }


}
