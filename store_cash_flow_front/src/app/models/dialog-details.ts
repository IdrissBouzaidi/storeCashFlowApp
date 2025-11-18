export class DialogDetails {
    id?: number;
    dialogIsVisible: boolean;
    dataSaved: boolean;

    constructor(id: number | undefined, dialogIsVisible: boolean | undefined, dataSaved: boolean | undefined) {
        this.id = id;
        this.dialogIsVisible = dialogIsVisible?? false;
        this.dataSaved = dataSaved?? false;
    }
}