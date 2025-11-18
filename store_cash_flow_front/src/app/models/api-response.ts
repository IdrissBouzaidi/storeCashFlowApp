export class ApiResponse<T> {
    success: boolean;
    data?: T;
    error?: string;

    constructor(dataOrError: T | Error) {
        if (dataOrError instanceof Error) {
            this.success = false;
            this.error = dataOrError.message;
        } else {
            this.success = true;
            this.data = dataOrError;
        }
    }
}