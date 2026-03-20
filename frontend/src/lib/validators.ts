const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const chinaPhonePattern = /^1[3-9]\d{9}$/
const strongPasswordPattern =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{12,128}$/

export const isValidEmail = (value: string) => emailPattern.test(value)

export const isValidChinaPhone = (value: string) => chinaPhonePattern.test(value)

export const isStrongPassword = (value: string) => strongPasswordPattern.test(value)

export const roleCodePattern = /^[A-Za-z][A-Za-z0-9_:-]{1,49}$/

export const isValidRoleCode = (value: string) => roleCodePattern.test(value)
