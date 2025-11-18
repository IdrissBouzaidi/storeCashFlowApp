export function convertNumberToFrNumber(num: string | number | undefined) {
    if(num ===  undefined)
        return undefined;
    else if(num === 0)
        return '0.00';
    let initNumStr: string = num.toString();
    if(!initNumStr.includes('.'))
        initNumStr += '.00';
    let numStr = initNumStr;
    numStr = numStr.substring(0, numStr.indexOf('.'));
    let length: number = numStr.length;
    let resultStr = '';
    for(let i = length-3; i>-3; i-=3) {
        const space: string = i === length - 3? '': ' ' ;//Pour les 3 premier nombres, on n'ajoute pas de séparateur.
        resultStr = (numStr.substring(Math.max(i, 0), i+3) + space) + resultStr;
    }
    let afterVirgule: string = initNumStr.substring(initNumStr.indexOf('.'));
    // Ajotuer les zéros à la fin
    if(afterVirgule.length>=1)
        afterVirgule += '00'.substring(0, 3 - afterVirgule.length);
    else
        afterVirgule = '.00';

    return resultStr + afterVirgule;
}