const TIME_VALUE_SELECT = {
    ALL: 1,
    TODAY: 2,
    THIS_WEEK: 3,
    THIS_MONTH: 4
};

const TIME_LABEL_SELECT = {
    1: "全部",
    2: "今日",
    3: "本周",
    4: "本月"
};

const  getTimeRangeWithFixdUnit = (unit: number) => {
    switch(unit) {  
        case TIME_VALUE_SELECT.TODAY: return "day";
        case TIME_VALUE_SELECT.THIS_WEEK: return "week";
        case TIME_VALUE_SELECT.THIS_MONTH: return "month";
        default: return "day";
    }
}

export default {TIME_LABEL_SELECT, TIME_VALUE_SELECT, getTimeRangeWithFixdUnit};
