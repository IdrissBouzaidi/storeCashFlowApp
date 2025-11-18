import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { FileDetails } from "../models/file-details";
import { ApiResponse } from "../models/api-response";
import { MIN_IO_API_URL, MIN_IO_FILES_URLS_URL } from "../utils/urls/external-urls";
import { Observable } from "rxjs";
import { RefTable } from "../models/ref-table";

@Injectable({
    providedIn: 'root'
})
export class MinIoService {
    private http = inject(HttpClient);

    updateImage(imageDetails: FileDetails): Observable<ApiResponse<string>> | undefined {
        const formData = new FormData();
        if(imageDetails.file && imageDetails.fileName) {
            debugger;
            formData.append('file', imageDetails.file, imageDetails.fileName);
            let params = new HttpParams();
            params = params.set('fileName', imageDetails.fileName);
            return this.http.post<ApiResponse<string>>(MIN_IO_API_URL, formData, {params});
        }
        return undefined;
    }

    getImagesUrls(imgSources: string[]) : Observable<ApiResponse<RefTable[]>>{
        debugger;
        let params = new HttpParams();
        params = params.appendAll({'fileNames': imgSources});
        return this.http.get<ApiResponse<RefTable[]>>(MIN_IO_FILES_URLS_URL, {params});
    }
}