package org.start2do.util;

public class GPSUtil {

    // 地球椭球参数（WGS84 坐标系）
    // 赤道半径（米）
    public static final double EQUATORIAL_RADIUS = 6378137.0;
    // 扁率
    public static final double FLATTENING = 1 / 298.257223563;
    // 极半径（米）
    public static final double POLAR_RADIUS = EQUATORIAL_RADIUS * (1 - FLATTENING);


    // Vincenty 公式计算距离（单位：米）
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将经纬度转换为弧度
        double phi1 = Math.toRadians(lat1);
        double lambda1 = Math.toRadians(lon1);
        double phi2 = Math.toRadians(lat2);
        double lambda2 = Math.toRadians(lon2);
        // 计算经度差
        double L = lambda2 - lambda1;
        // 初始参数
        double U1 = Math.atan((1 - FLATTENING) * Math.tan(phi1));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(phi2));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
        // 迭代计算
        double lambda = L;
        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambdaPrev;
        int iterations = 0;
        final int MAX_ITERATIONS = 200;
        // 收敛阈值
        final double TOLERANCE = 1e-12;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(
                Math.pow(cosU2 * sinLambda, 2) + Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * cosLambda, 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = (cosU1 * cosU2 * sinLambda) / sinSigma;
            cosSqAlpha = 1 - Math.pow(sinAlpha, 2);
            cos2SigmaM = (cosSqAlpha == 0) ? 0 : cosSigma - (2 * sinU1 * sinU2) / cosSqAlpha;
            double C = (FLATTENING / 16) * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            lambdaPrev = lambda;
            lambda = L + (1 - C) * FLATTENING * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2
                                                                                                                      * Math.pow(
                cos2SigmaM, 2))));
            iterations++;
        } while (Math.abs(lambda - lambdaPrev) > TOLERANCE && iterations < MAX_ITERATIONS);
        // 计算椭球上的距离
        double uSq =
            cosSqAlpha * (Math.pow(EQUATORIAL_RADIUS, 2) - Math.pow(POLAR_RADIUS, 2)) / Math.pow(POLAR_RADIUS, 2);
        double A = 1 + (uSq / 16384) * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = (uSq / 1024) * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + (B / 4) * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                                                                    - (B / 6) * cos2SigmaM * (-3 + 4 * Math.pow(
            sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));
        return POLAR_RADIUS * A * (sigma - deltaSigma);
    }

    public static void main(String[] args) {
        // 示例：北京天安门 (WGS84) 到上海外滩 (WGS84)
        double lat1 = 39.9042, lon1 = 116.4074;
        double lat2 = 31.2304, lon2 = 121.4737;
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        // 约 1,068,000 米（1068公里）
        System.out.println("距离: " + distance + " 米");
    }
}
