package processdataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*****
 * ������⵽Ƶ�ʽ��ٵ�ap �Ƿ�Ӧ����һЩ��������������Ϊ��⵽�Ĵ�����
 *****/

public class TestPositioning {
	static OfflineData offline;
	static OnlineData online;
	static Result results = new Result();
	static List<String> aplist = Arrays.asList(Constant.AP_ARR);// APLIST����ͳһʹ��offline��

	public static void main(String[] args) {
		// (27,-99,-100)WKNN k=4 -1.423 KNN-1.428 1.4219 avail��-95�����ȽϺ�
		OfflineData.Options options = new OfflineData.Options(1.0, 1.0, 27, -99, -100);
		offline = new OfflineData(Constant.OFF_PATH, options);
		online = new OnlineData(Constant.ON_PATH, 1.0);

		// ��46�����µ���ж�λ
		for (int c = 0; c < 46; c++) {
//			 KNN(online.avgRssList.get(c), 4, true, c);//5,6������� 4+true��� 1.5306
//			 WKNN(online.avgRssList.get(c), 4, 0.1, true, c);//6������� 4+true 1.5297 exp=0.1:1.4217
			
			 histogramOnePos(offline.rssVectors, online.allRss.get(c), 4, 0.1, c);
		}
		results.calculateOverAllDeviationAndVariance();
	}

	/**
	 * ��K-nearest neighbor���ж�λ
	 * @param onrss һ�����ϵ��RSS
	 * @param k
	 * @param usePenalty �Ƿ�ʹ��penalty
	 * @param index ��ʾ�ڼ������ϵ�
	 */
	public static void KNN(Map<String, Double> onrss, int k, boolean usePenalty, int index) {
		Map<Double, Integer> distanceMap = new TreeMap<>();// λ�ã�0-129��-����
		int p = 0;
		for (int i = 0; i < Constant.OFF_POS_ARR.length; i++) {
			Map<String, Double> offrss = offline.avgRssList.get(i);
			Map<String, Double> penaltyMap = offline.penaltyList.get(i);
			double distance, sum = 0;
			for (int j = 0; j < aplist.size(); j++) {
				double penalty = penaltyMap.get(aplist.get(j));
				double off, on;
				if (offrss.get(aplist.get(j)) != null)
					off = offrss.get(aplist.get(j));
				else
					off = -100.0;

				if (onrss.get(aplist.get(j)) != null)
					on = onrss.get(aplist.get(j));
				else
					on = -100.0;
				sum += usePenalty ? penalty * (off - on) * (off - on) : (off - on) * (off - on);
			}
			distance = Math.sqrt(sum);
			distanceMap.put(distance, i);
			// System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//���off��on�ľ���
		}

		double xsum = 0.0, ysum = 0.0;
		int kk = 0;
		for (Double d : distanceMap.keySet()) {
			if (kk++ == k)
				break;
			int pos = distanceMap.get(d);
			xsum += offline.points[pos].x;
			ysum += offline.points[pos].y;
			System.out.println(offline.points[pos] + " d=" + d);
		}
		Point result = new Point(xsum / k, ysum / k);
		results.addResult(result, online.points[index].distance(result));
		System.out.println("result:" + result + "   real:" + online.points[index] + " deviation:"
				+ online.points[index].distance(result));
	}

	/**
	 * ���ݾ���Ĳ�ͬ����Ȩ�� Ч��һ���KNN��һ��
	 * @param onrss
	 * @param k
	 * @param exp Ȩ�صĴη���
	 * @param usePenalty
	 * @param index
	 */
	public static void WKNN(Map<String, Double> onrss, int k, double exp, boolean usePenalty, int index) {
		Map<Double, Integer> distanceMap = new TreeMap<>();// λ�ã�0-129��-����
		for (int i = 0; i < Constant.OFF_POS_ARR.length; i++) {
			// ������� ֻ�������³��ֵ�AP ��������ֻ����һ���� Ĭ����һ��Ϊ-100
			Map<String, Double> offrss = offline.avgRssList.get(i);
			Map<String, Double> penaltyMap = offline.penaltyList.get(i);
			double distance, sum = 0;
			for (int j = 0; j < aplist.size(); j++) {
				double penalty = penaltyMap.get(aplist.get(j));
				double off, on;
				if (offrss.get(aplist.get(j)) != null)
					off = offrss.get(aplist.get(j));
				else
					off = -100.0;

				if (onrss.get(aplist.get(j)) != null)
					on = onrss.get(aplist.get(j));
				else
					on = -100.0;
				sum += usePenalty ? penalty * (off - on) * (off - on) : (off - on) * (off - on);
			}
			distance = Math.sqrt(sum);
			distanceMap.put(distance, i);
			// System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//���off��on�ľ���
		}

		double x = 0.0, y = 0.0, weight = 0.0;
		double[] weights = new double[k];
		Iterator<Map.Entry<Double, Integer>> iterator = distanceMap.entrySet().iterator();
		for (int a = 0; a < k; a++) {
			Map.Entry<Double, Integer> entry = iterator.next();
			int pos = entry.getValue();
			weights[a] = Math.pow(1 / entry.getKey(), exp);
			weight += Math.pow(1 / entry.getKey(), exp);
			// System.out.println(offline.XArr[pos]+","+offline.YArr[pos]+" d="+entry.getKey()+" weight="+weights[a]);
		}
		
		// System.out.println("total weight="+weight);
		Iterator<Map.Entry<Double, Integer>> entries = distanceMap.entrySet().iterator();
		for (int b = 0; b < k; b++) {
			Map.Entry<Double, Integer> entry = entries.next();
			int pos = entry.getValue();
			x += offline.points[pos].x * weights[b] / weight;
			y += offline.points[pos].y * weights[b] / weight;
			// System.out.println(Constant.OFF_POS_ARR[pos]+" d="+entry.getKey()+" w="+weights[b]);
		}
		Point result = new Point(x, y);
		results.addResult(result, online.points[index].distance(result));
		System.out.println("result:" + result + "   real:" + online.points[index] + " deviation:"
				+ online.points[index].distance(result));
	}

	/**
	 * ��ֱ��ͼ��λ �����㷨��online��ÿһ����ÿһ�εĶ�λ����������
	 * @param rssVectorlist �������е�offlineֱ��ͼ
	 * @param online ��������
	 */
	public static void histogramOnePos(List<List<TreeMap<Double, Integer>>> rssVectorlist,
			List<Map<String, Double>> onePosRss, int k, double exp, int c) {
		System.out.println(Constant.ON_POS_ARR[c] + "******************");
		double[] deviations = new double[onePosRss.size()];
		Point[] resultPoints= new Point[onePosRss.size()];
		for (int i = 0; i < onePosRss.size(); i++) {//
			Map<String, Double> oneTimeRss = onePosRss.get(i);
			resultPoints[i] = probOneTime(rssVectorlist, oneTimeRss, k, exp, false);
			deviations[i] = resultPoints[i].distance(online.points[c]);
//			System.out.println("time " + i + "    result" + resultPoints[i] + "   deviation:" + deviations[i]);
		}
		double xsum=0.0, ysum=0.0;
		for (int i=0;i<resultPoints.length;i++){
			xsum+=resultPoints[i].x;
			ysum+=resultPoints[i].y;
		}
		Point p=new Point(xsum/resultPoints.length,ysum/resultPoints.length);
		results.addResult(p, p.distance(online.points[c]));
		System.out.println("result"+ p + "one point deviation:"+p.distance(online.points[c]));
	}

	/**
	 * ȡ��������n���� Ȼ��������kNN/wkNN
	 * @param rssVectorlist
	 * @param oneTimeRss
	 */
	public static Point probOneTime(List<List<TreeMap<Double, Integer>>> rssVectorlist, Map<String, Double> oneTimeRss,
			int k, double exp, boolean isWeight) {
		TreeMap<Double, Integer> probMap = new TreeMap<>();// ÿ�����<���ʡ�������>
		for (int i = 0; i < rssVectorlist.size(); i++) {// �����µ����е�
			List<TreeMap<Double, Integer>> onePosHistogram = rssVectorlist.get(i);
			double p = 1.0;// �����ĺ���ָ�� �����Ǹ���
			int j = 0;
			for (String ap : aplist) {
				Double onrss = oneTimeRss.get(ap);// �᲻��Ϊ�գ�
				if (onrss != null) {
					Map<Double, Integer> rssVector = onePosHistogram.get(j);
					Integer times = rssVector.get(onrss);
					if (times != null)
						p *= times;
				}
				j++;
			}
			probMap.put(p, i);
		}

		double x = 0, y = 0, weight = 0.0;
		int i = 0;
		double[] weights = new double[k];
		Iterator<Map.Entry<Double, Integer>> iterator = probMap.entrySet().iterator();
		for (int a = 0; a < k; a++) {
			Map.Entry<Double, Integer> entry = iterator.next();
			int pos = entry.getValue();
			weights[a] = Math.pow(1 / entry.getKey(), exp);
			weight += weights[a];
			// System.out.println(offline.XArr[pos]+","+offline.YArr[pos]+" d="+entry.getKey()+" weight="+weights[a]);
		}
		// System.out.println("total weight="+weight);

		for (double prob : probMap.descendingKeySet()) {
			if (i == k)
				break;
			int index = probMap.get(prob);
			// System.out.println(i+" "+Constant.OFF_POS_ARR[index]+" prob="+(prob));
			x += offline.points[index].x * weights[i] / weight;
			y += offline.points[index].y * weights[i++] / weight;
		}

		return new Point(x, y);
	}
}
