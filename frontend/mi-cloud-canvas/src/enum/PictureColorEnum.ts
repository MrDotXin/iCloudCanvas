const PIC_CATEGORY_COLOR_MAP: { [key: string]: string } = {
    "插画": "#FF6B6B",  // 温暖珊瑚色（适合艺术类）
    "科幻": "#4ECDC4",  // 科技感蓝绿色
    "风景": "#06D6A0",  // 自然绿色
    "城市": "#FFD166",  // 城市灯光黄色
    "设计": "#A78BFA",  // 创意紫色
    "表情包": "#FF85C0", // 活泼粉色
    "艺术": "#A3E4D7",  // 艺术感浅青
    "海报": "#FAA381"   // 醒目橙红色
};

const PIC_TAG_COLOR_MAP: { [key: string]: string } = {
    "高清": "#7FB3D5",  // 清晰感浅蓝
    "二次元": "#FF9FF3", // 动漫风格粉
    "校园": "#A5D8A7",   // 青春绿色
    "热门": "#FF7F7F",   // 热度红色
    "生活": "#FECA57",   // 生活化橙色
    "自然": "#48C774",   // 自然深绿
    "屌图": "#BDC3C7",   // 中性灰色（幽默类）
    "4k": "#5C6BC0",    // 高清技术蓝
    "美食": "#F368E0"    // 食欲紫色
};

const PIC_REVIEW_TAG_MAP: { [key: number]: string } = {
    0: "#054bc6", 
    1: "#14dc06",   
    2: "#c60505",  
};

export default {PIC_CATEGORY_COLOR_MAP, PIC_TAG_COLOR_MAP, PIC_REVIEW_TAG_MAP};
