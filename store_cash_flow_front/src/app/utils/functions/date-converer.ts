import { DatePipe } from "@angular/common";
import { inject } from "@angular/core";

export function convertServiceDateToDateObject(dateStr: string) {

    const [year, month, day] = dateStr.split("-");
    const dateObj: Date = new Date(Number(year), Number(month) - 1, Number(day));
    return dateObj;
}

export function convertServiceTimeToTimeObject(timeStr: string) {
    const [hour, minute, second] = timeStr.split(":");
    const dateObj = new Date();
    dateObj.setHours(Number(hour));
    dateObj.setMinutes(Number(minute));
    dateObj.setSeconds(Number(second));
    return dateObj;
}

export function convertDateObjectToServiceDate(date: Date) {
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    const hour = date.getHours();
    const minute = date.getMinutes();
    const second = date.getSeconds();

    return '' + day + '-' + month + '-' + year + ' ' + hour + ':' + minute + ':' + second;
}

export function convertTimeToString(time: Date) {
    return time.toTimeString().substring(0, 8);
}

export function convertDateAndTimeToLisibleString(date: Date) {
    const datePipe = new DatePipe('en-US');
    return datePipe.transform(date, 'yyyyMMdd_hhmmss_SSS');
}