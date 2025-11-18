import { inject } from "@angular/core";
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from "../service/auth.service";
import { UserService } from "../service/user.service";
import { VIEW_ADVANCE_ACTION, VIEW_ALL_OPERATIONS_ACTION, VIEW_CAPITAL_CONTRIBUTION_ACTION, VIEW_CHARGE_ACTION, VIEW_CONS_INPUTS_ACTION, VIEW_CUSTOMER_CREDIT_ACTION, VIEW_EXTERNAL_LOAN_ACTION, VIEW_NOT_CONS_INPUT_ACTION, VIEW_OUT_OF_POCKET_EXPENSE_ACTION, VIEW_OUTPUT_ACTION } from "./consts/actions-consts";
import { HOME_INTERNAL_URL } from "./urls/internal-urls";

export const authGuard = (route?: ActivatedRouteSnapshot, state?: RouterStateSnapshot) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    let val = auth.check();
    return auth.check() ? true : router.parseUrl('/auth/login');
};

export const viewAllOperationsGuard = (route?: ActivatedRouteSnapshot, state?: RouterStateSnapshot) => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_ALL_OPERATIONS_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewConsInputsGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_CONS_INPUTS_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewNotConsInputGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_NOT_CONS_INPUT_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewOutputGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_OUTPUT_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewChargeGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_CHARGE_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewCapitalContributionGuard = () => {
    debugger;
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    const val = userService.getActions()?.some(action => action.id === VIEW_CAPITAL_CONTRIBUTION_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
    return userService.getActions()?.some(action => action.id === VIEW_CAPITAL_CONTRIBUTION_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewAdvanceGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_ADVANCE_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewOutOfPocketExpenseGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_OUT_OF_POCKET_EXPENSE_ACTION)?true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewCustomerCreditGuard = () => {
    const userService: UserService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_CUSTOMER_CREDIT_ACTION)? true: router.parseUrl(HOME_INTERNAL_URL);
}

export const viewExternalLoanGuard = () => {
    const userService = inject(UserService);
    const router = inject(Router);
    return userService.getActions()?.some(action => action.id === VIEW_EXTERNAL_LOAN_ACTION)? true: router.parseUrl(HOME_INTERNAL_URL);
}