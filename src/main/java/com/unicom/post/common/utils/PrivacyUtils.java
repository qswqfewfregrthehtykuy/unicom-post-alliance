package com.unicom.post.common.utils;

/**
 * 隐私数据脱敏工具类
 */
public class PrivacyUtils {

    /**
     * 手机号脱敏：保留前3位和后4位，中间用****替代
     * 例：13812345678 → 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏：保留前4位和后4位，中间用*替代
     * 例：320102199001011234 → 3201**********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.replaceAll("(\\w{4})\\w+(\\w{4})", "$1**********$2");
    }

    /**
     * 姓名脱敏：保留姓，名用*替代
     * 例：张三 → 张*
     * 例：张三丰 → 张**
     * 例：欧阳锋 → 欧阳*
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        // 复姓处理（常见复姓长度为2）
        int surnameLen = isCompoundSurname(name) ? 2 : 1;
        String surname = name.substring(0, surnameLen);
        String givenNameMasked = name.substring(surnameLen).replaceAll(".", "*");
        return surname + givenNameMasked;
    }

    /**
     * 邮箱脱敏：保留首字母和域名，中间用***替代
     * 例：zhangsan@example.com → z***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        if (localPart.length() <= 1) {
            return localPart + "***@" + domain;
        }
        return localPart.charAt(0) + "***@" + domain;
    }

    /**
     * 根据脱敏类型自动脱敏
     */
    public static String mask(String value, String maskType) {
        if (value == null) return null;
        switch (maskType.toUpperCase()) {
            case "PHONE":   return maskPhone(value);
            case "ID_CARD": return maskIdCard(value);
            case "NAME":    return maskName(value);
            case "EMAIL":   return maskEmail(value);
            default:        return value;
        }
    }

    /**
     * 判断是否为常见复姓
     */
    private static boolean isCompoundSurname(String name) {
        if (name.length() < 2) return false;
        String prefix = name.substring(0, 2);
        return "欧阳".equals(prefix) || "太史".equals(prefix) || "端木".equals(prefix)
                || "上官".equals(prefix) || "司马".equals(prefix) || "东方".equals(prefix)
                || "独孤".equals(prefix) || "南宫".equals(prefix) || "万俟".equals(prefix)
                || "闻人".equals(prefix) || "夏侯".equals(prefix) || "诸葛".equals(prefix)
                || "尉迟".equals(prefix) || "公羊".equals(prefix) || "赫连".equals(prefix)
                || "澹台".equals(prefix) || "皇甫".equals(prefix) || "宗政".equals(prefix)
                || "濮阳".equals(prefix) || "公冶".equals(prefix) || "太叔".equals(prefix)
                || "申屠".equals(prefix) || "公孙".equals(prefix) || "慕容".equals(prefix)
                || "仲孙".equals(prefix) || "钟离".equals(prefix) || "长孙".equals(prefix)
                || "宇文".equals(prefix) || "司徒".equals(prefix) || "鲜于".equals(prefix)
                || "司空".equals(prefix) || "闾丘".equals(prefix) || "子车".equals(prefix)
                || "亓官".equals(prefix) || "司寇".equals(prefix) || "巫马".equals(prefix)
                || "公西".equals(prefix) || "颛孙".equals(prefix) || "壤驷".equals(prefix)
                || "公良".equals(prefix) || "漆雕".equals(prefix) || "乐正".equals(prefix)
                || "宰父".equals(prefix) || "谷梁".equals(prefix) || "拓跋".equals(prefix)
                || "夹谷".equals(prefix) || "轩辕".equals(prefix) || "令狐".equals(prefix)
                || "段干".equals(prefix) || "百里".equals(prefix) || "呼延".equals(prefix)
                || "东郭".equals(prefix) || "南门".equals(prefix) || "羊舌".equals(prefix)
                || "微生".equals(prefix) || "公户".equals(prefix) || "公玉".equals(prefix)
                || "公仪".equals(prefix) || "梁丘".equals(prefix) || "公仲".equals(prefix)
                || "公上".equals(prefix) || "公门".equals(prefix) || "公山".equals(prefix)
                || "公坚".equals(prefix) || "左丘".equals(prefix) || "公伯".equals(prefix)
                || "西门".equals(prefix) || "公祖".equals(prefix) || "第五".equals(prefix)
                || "公乘".equals(prefix) || "贯丘".equals(prefix) || "公皙".equals(prefix)
                || "南荣".equals(prefix) || "东里".equals(prefix) || "东宫".equals(prefix)
                || "仲长".equals(prefix) || "子书".equals(prefix) || "子桑".equals(prefix)
                || "即墨".equals(prefix) || "达奚".equals(prefix) || "褚师".equals(prefix);
    }
}
