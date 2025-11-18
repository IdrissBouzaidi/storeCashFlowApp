export class FileDetails {
    file?: File;
    fileName?: string;
    fileInternalUrl?: string | ArrayBuffer | null;
    uploadInfo: any;

    constructor(file?: File, fileName?: string) {
        this.file = file;
        this.fileName = fileName;
    }
}