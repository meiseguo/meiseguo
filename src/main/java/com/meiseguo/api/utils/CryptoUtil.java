package com.meiseguo.api.utils;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.*;
import org.springframework.util.ObjectUtils;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CryptoUtil {
    public static Random random = new Random();
    private final static ImmutableList<ChildNumber> BIP44_ETH_ACCOUNT_ZERO_PATH =
            ImmutableList.of(new ChildNumber(44, true), new ChildNumber(60, true),
                    ChildNumber.ZERO_HARDENED, ChildNumber.ZERO);
    public static final String inviteSymbol = "0123456789@ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String hashSymbol = "0123456789@abcdefghijklmnopqrstuvwxyz";
    public static final String codeSymbol = "0123456789";
    public static String md5(String text) {
        if(text == null) {
            return null;
        }
        try {
            MessageDigest  digest = MessageDigest.getInstance("MD5");
            byte[]  bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new  StringBuffer();
            for(int i = 0;i<bytes.length;i++){
                String s = Integer.toHexString(0xff&bytes[i]);
                if(s.length()==1){
                    sb.append("0"+s);
                }else{
                    sb.append(s);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("no shit");
        }
    }
    protected static String substring(String s, int from) {
        try {
            return s.substring(from);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    protected static String substring(String s, int from, int len) {
        try {
            return s.substring(from, from + len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String cleanPhone(String telNum) {
        telNum = telNum.replace("-", "");
        telNum = telNum.replace(" ", "");

        if (substring(telNum, 0, 4).equals("0086"))
            telNum = substring(telNum, 4);
        else if (substring(telNum, 0, 3).equals("+86"))
            telNum = substring(telNum, 3);
        else if (substring(telNum, 0, 5).equals("00186"))
            telNum = substring(telNum, 5);

        return telNum;
    }
    public static String cleanXSS(String valueP) {
        // You'll need to remove the spaces from the html entities below
        String value = valueP.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
        value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
        value = value.replaceAll("'", "& #39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        return value;
    }

    public static String randomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        while(length -- > 0) {
            sb.append(inviteSymbol.charAt(random.nextInt(inviteSymbol.length())));
        }
        return sb.toString();
    }

    public static String hashCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        while(length -- > 0) {
            sb.append(hashSymbol.charAt(random.nextInt(hashSymbol.length())));
        }
        return sb.toString();
    }

    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public static String randomStr() {
        return Long.toHexString(random.nextLong()*2);
    }
    public static String randomInt() {
        return String.valueOf((int)((Math.random()*9+1)*100000));
    }

    public static long expire(long ms) {
        return System.currentTimeMillis() + ms;
    }
    public static boolean expired(long ms) {
        return System.currentTimeMillis() > ms;
    }

    /**
     * unique order id
     * @param number
     * @return
     */
    public static String uniqueid(String format, long number) {
        return new DecimalFormat(format).format(number);
    }


    public static String buildMsg(Map<String, String> data, String template) {
        for(String key : data.keySet()) {
            String replace = "{" + key + "}";
            String value = data.get(key);
            template = template.replace(replace, value);
        }
        return template;
    }

    public static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static String date(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static int randomNum(int i) {
        return random.nextInt(i);
    }


    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    public  static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }
    /**
     * sha256_HMAC加密
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    public static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    public static String phone(String phone) {
        if(ObjectUtils.isEmpty(phone)) return "@***********";
        return "@" + phone.substring(0,3) + "*******" + phone.charAt(phone.length()-1);
    }

    public static Collection<String> appendList(String avatar, String images) {
        Collection<String> data = new ArrayList<>();
        data.add(avatar);
        if(!ObjectUtils.isEmpty(images)) {
            String[] args = images.split(",");
            data.addAll(Arrays.asList(args));
        }
        return data;
    }

    public static String repeat(String symbol, int sum) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<sum;i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static String[] split(String s, String images) {
        if(ObjectUtils.isEmpty(images)) return new String[0];
        return images.split(s);
    }


    static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";
    public static boolean checkEthSign(String message, String address, String signature) {
        String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
        byte[] msgHash = Hash.sha3((prefix + message).getBytes());

        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }
        Sign.SignatureData sd = new Sign.SignatureData(v, Arrays.copyOfRange(signatureBytes, 0, 32), Arrays.copyOfRange(signatureBytes, 32, 64));

        for (int i = 0; i < 4; i++) {
            BigInteger publicKey = Sign.recoverFromSignature((byte) i, new ECDSASignature( new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())), msgHash);
            if (publicKey != null) {
                String addressRecovered = "0x" + Keys.getAddress(publicKey);
                if (addressRecovered.equalsIgnoreCase(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String de(String encoded) {
        char[] chars = encoded.toCharArray();
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for(char c: chars) {
            index++;
            int i = Character.digit(c, 16) -1;
            if(i<0) sb.append(c);
            else sb.append(index>2?String.valueOf(i):c);
        }
        return sb.toString();
    }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    public static String currentMs() {
        String millis = "" + System.currentTimeMillis();
        try {
            TimeUnit.MICROSECONDS.sleep(1);
        } catch (InterruptedException e) {
            //ignore
        }
        return "1"+millis.substring(3);
    }


    public static boolean chance(double chance) {
        return random.nextDouble() < chance;
    }
}
