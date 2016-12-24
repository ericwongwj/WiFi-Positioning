package processdataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*****
 * 对于侦测到频率较少的ap 是否应当做一些处理？例如向量变为侦测到的次数？
 *****/

public class TestPositioning {
	static OfflineData offline;
	static OnlineData online;
	static Result results = new Result();
	static List<String> aplist = Arrays.asList(Constant.AP_ARR);// APLIST必须统一使用offline的

	public static void main(String[] args) {
		// (27,-99,-100)WKNN k=4 -1.423 KNN-1.428 1.4219 avail在-95附近比较好
		OfflineData.Options options = new OfflineData.Options(1.0, 1.0, 27, -99, -100);
		offline = new OfflineData(Constant.OFF_PATH, options);
		online = new OnlineData(Constant.ON_PATH, 1.0);

		// 对46个线下点进行定位
		for (int c = 0; c < 46; c++) {
//			 KNN(online.avgRssList.get(c), 4, true, c);//5,6精度最好 4+true最好 1.5306
//			 WKNN(online.avgRssList.get(c), 4, 0.1, true, c);//6精度最好 4+true 1.5297 exp=0.1:1.4217
			
			 histogramOnePos(offline.rssVectors, online.allRss.get(c), 4, 0.1, c);
		}
		results.calculateOverAllDeviationAndVariance();
	}

	/**
	 * 用K-nearest neighbor进行定位
	 * @param onrss 一个线上点的RSS
	 * @param k
	 * @param usePenalty 是否使用penalty
	 * @param index 表示第几个线上点
	 */
	public static void KNN(Map<String, Double> onrss, int k, boolean usePenalty, int index) {
		Map<Double, Integer> distanceMap = new TreeMap<>();// 位置（0-129）-距离
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
			// System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
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
	 * 根据距离的不同加上权重 效果一般比KNN好一点
	 * @param onrss
	 * @param k
	 * @param exp 权重的次方数
	 * @param usePenalty
	 * @param index
	 */
	public static void WKNN(Map<String, Double> onrss, int k, double exp, boolean usePenalty, int index) {
		Map<Double, Integer> distanceMap = new TreeMap<>();// 位置（0-129）-距离
		for (int i = 0; i < Constant.OFF_POS_ARR.length; i++) {
			// 计算距离 只计算线下出现的AP 线上线下只出现一个的 默认另一个为-100
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
			// System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
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
	 * 用直方图定位 概率算法对online的每一个点每一次的定位结果进行输出
	 * @param rssVectorlist 包含所有的offline直方图
	 * @param online 线上数据
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
	 * 取概率最大的n个点 然后再类似kNN/wkNN
	 * @param rssVectorlist
	 * @param oneTimeRss
	 */
	public static Point probOneTime(List<List<TreeMap<Double, Integer>>> rssVectorlist, Map<String, Double> oneTimeRss,
			int k, double exp, boolean isWeight) {
		TreeMap<Double, Integer> probMap = new TreeMap<>();// 每个点的<概率——点编号>
		for (int i = 0; i < rssVectorlist.size(); i++) {// 对线下的所有点
			List<TreeMap<Double, Integer>> onePosHistogram = rssVectorlist.get(i);
			double p = 1.0;// 这个点的衡量指标 可以是概率
			int j = 0;
			for (String ap : aplist) {
				Double onrss = oneTimeRss.get(ap);// 会不会为空？
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
