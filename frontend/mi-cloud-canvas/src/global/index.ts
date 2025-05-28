import { saveAs } from 'file-saver';
const sleep = (ms: number) => {
    return new Promise(resolve => setTimeout(resolve, ms));
};

export const formatSize = (size?: number) => {  
    if (!size) return '未知'  
    if (size < 1024) return size + ' B'  
    if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'  
    return (size / (1024 * 1024)).toFixed(2) + ' MB'  
}

export const doFileDownload = (url : string, filename : string) => {   
    saveAs(url, filename);
}

export const global = {
    sleep,
};