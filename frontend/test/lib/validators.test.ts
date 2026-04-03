import { describe, expect, it } from 'vitest'
import {
  isValidEmail,
  isValidChinaPhone,
  isStrongPassword,
  isValidRoleCode
} from '@/lib/validators'

describe('validators', () => {
  // =========================================================================
  // 1. isValidEmail
  // =========================================================================
  describe('isValidEmail', () => {
    it('accepts valid email addresses', () => {
      expect(isValidEmail('user@example.com')).toBe(true)
      expect(isValidEmail('test.user@domain.co.uk')).toBe(true)
      expect(isValidEmail('admin+tag@sub.domain.org')).toBe(true)
      expect(isValidEmail('a@b.c')).toBe(true)
    })

    it('rejects invalid email addresses', () => {
      expect(isValidEmail('')).toBe(false)
      expect(isValidEmail('not-an-email')).toBe(false)
      expect(isValidEmail('missing@domain')).toBe(false)
      expect(isValidEmail('@missing-local.com')).toBe(false)
      expect(isValidEmail('has space@example.com')).toBe(false)
      expect(isValidEmail('double@@at.com')).toBe(false)
      expect(isValidEmail('no-tld@domain')).toBe(false)
    })

    it('handles edge cases', () => {
      // IP addresses are actually valid by this pattern
      expect(isValidEmail('user@127.0.0.1')).toBe(true)
      expect(isValidEmail('user+plus+tag@example.com')).toBe(true)
      expect(isValidEmail('user.name+tag+label@example.co.uk')).toBe(true)
    })
  })

  // =========================================================================
  // 2. isValidChinaPhone
  // =========================================================================
  describe('isValidChinaPhone', () => {
    it('accepts valid China mobile numbers', () => {
      expect(isValidChinaPhone('13812345678')).toBe(true)
      expect(isValidChinaPhone('15987654321')).toBe(true)
      expect(isValidChinaPhone('18600000000')).toBe(true)
      expect(isValidChinaPhone('19123456789')).toBe(true) // 19x is valid
    })

    it('rejects invalid phone numbers', () => {
      expect(isValidChinaPhone('')).toBe(false)
      expect(isValidChinaPhone('12345678901')).toBe(false) // starts with 12
      expect(isValidChinaPhone('1381234567')).toBe(false) // too short
      expect(isValidChinaPhone('138123456789')).toBe(false) // too long
      expect(isValidChinaPhone('10812345678')).toBe(false) // starts with 10
      expect(isValidChinaPhone('abcdefghijk')).toBe(false) // non-numeric
      expect(isValidChinaPhone('138-1234-5678')).toBe(false) // has dashes
    })

    it('validates China mobile prefix rules', () => {
      // Valid prefixes: 13x, 14x, 15x, 16x, 17x, 18x, 19x
      // China phone numbers are 11 digits: 1 + prefix digit (3-9) + 9 more digits
      const validPrefixes = ['130', '131', '132', '133', '134', '135', '136', '137', '138', '139',
        '140', '141', '142', '143', '144', '145', '146', '147', '148', '149',
        '150', '151', '152', '153', '154', '155', '156', '157', '158', '159',
        '160', '161', '162', '163', '164', '165', '166', '167', '168', '169',
        '170', '171', '172', '173', '174', '175', '176', '177', '178', '179',
        '180', '181', '182', '183', '184', '185', '186', '187', '188', '189',
        '190', '191', '192', '193', '194', '195', '196', '197', '198', '199']

      validPrefixes.forEach(prefix => {
        // Need 8 more digits after prefix to make 11 total
        expect(isValidChinaPhone(prefix + '12345678')).toBe(true)
      })

      // Invalid prefixes (not 13-19)
      expect(isValidChinaPhone('12012345678')).toBe(false)
      expect(isValidChinaPhone('10012345678')).toBe(false)
      expect(isValidChinaPhone('11012345678')).toBe(false)
    })
  })

  // =========================================================================
  // 3. isStrongPassword
  // =========================================================================
  describe('isStrongPassword', () => {
    it('accepts strong passwords', () => {
      expect(isStrongPassword('Abcdef123!@#')).toBe(true)
      expect(isStrongPassword('Password123!')).toBe(true)
      expect(isStrongPassword('StrongP@ssw0rd')).toBe(true)
      expect(isStrongPassword('MySecr3t!Pass')).toBe(true)
    })

    it('rejects passwords without uppercase letter', () => {
      expect(isStrongPassword('abcdef123!@#')).toBe(false)
      expect(isStrongPassword('password123!')).toBe(false)
    })

    it('rejects passwords without lowercase letter', () => {
      expect(isStrongPassword('ABCDEF123!@#')).toBe(false)
      expect(isStrongPassword('PASSWORD123!')).toBe(false)
    })

    it('rejects passwords without digit', () => {
      expect(isStrongPassword('Abcdefgh!@#')).toBe(false)
      expect(isStrongPassword('Password!!!')).toBe(false)
    })

    it('rejects passwords without special character', () => {
      expect(isStrongPassword('Abcdef123456')).toBe(false)
      expect(isStrongPassword('Password123')).toBe(false)
    })

    it('enforces minimum length of 12 characters', () => {
      // Too short
      expect(isStrongPassword('Abc123!@')).toBe(false) // 8 chars
      expect(isStrongPassword('Abcdef1!')).toBe(false) // 8 chars
      expect(isStrongPassword('Abcdef12!')).toBe(false) // 10 chars
      expect(isStrongPassword('Abcdef123!')).toBe(false) // 11 chars

      // Valid lengths (12 or more characters)
      expect(isStrongPassword('Abcdef123!@#')).toBe(true) // 12 chars - exactly minimum
      expect(isStrongPassword('Abcdef12345!@')).toBe(true) // 13 chars
      expect(isStrongPassword('Abcdef123456789!@#')).toBe(true) // 19 chars
    })

    it('enforces maximum length of 128 characters', () => {
      const longPassword = 'Aa1!' + 'x'.repeat(125) // 129 chars total
      expect(isStrongPassword(longPassword)).toBe(false)

      const validLongPassword = 'Aa1!' + 'x'.repeat(124) // 128 chars total
      expect(isStrongPassword(validLongPassword)).toBe(true)
    })

    it('accepts various special characters', () => {
      expect(isStrongPassword('Password123!')).toBe(true)
      expect(isStrongPassword('Password123@')).toBe(true)
      expect(isStrongPassword('Password123#')).toBe(true)
      expect(isStrongPassword('Password123$')).toBe(true)
      expect(isStrongPassword('Password123%')).toBe(true)
      expect(isStrongPassword('Password123^')).toBe(true)
      expect(isStrongPassword('Password123&')).toBe(true)
      expect(isStrongPassword('Password123*')).toBe(true)
      expect(isStrongPassword('Password123(')).toBe(true)
      expect(isStrongPassword('Password123)')).toBe(true)
    })
  })

  // =========================================================================
  // 4. isValidRoleCode
  // =========================================================================
  describe('isValidRoleCode', () => {
    it('accepts valid role codes', () => {
      expect(isValidRoleCode('ROLE_ADMIN')).toBe(true)
      expect(isValidRoleCode('RA')).toBe(true) // 2 chars minimum
      expect(isValidRoleCode('role_user')).toBe(true)
      expect(isValidRoleCode('MixedCase_123')).toBe(true)
      expect(isValidRoleCode('A:B:C-D_E')).toBe(true)
    })

    it('rejects invalid role codes', () => {
      expect(isValidRoleCode('')).toBe(false)
      expect(isValidRoleCode('A')).toBe(false) // only 1 char
      expect(isValidRoleCode('1startsWithNumber')).toBe(false)
      expect(isValidRoleCode('_startsWithUnderscore')).toBe(false)
      expect(isValidRoleCode(':startsWithColon')).toBe(false)
      expect(isValidRoleCode('-startsWithDash')).toBe(false)
      expect(isValidRoleCode('has space')).toBe(false)
      expect(isValidRoleCode('has.dot')).toBe(false)
      expect(isValidRoleCode('has/slash')).toBe(false)
    })

    it('enforces maximum length of 50 characters', () => {
      const validCode = 'A' + 'a'.repeat(49) // 50 chars
      expect(isValidRoleCode(validCode)).toBe(true)

      const invalidCode = 'A' + 'a'.repeat(50) // 51 chars
      expect(isValidRoleCode(invalidCode)).toBe(false)
    })

    it('allows special characters in middle', () => {
      expect(isValidRoleCode('ROLE_ADMIN_USER')).toBe(true)
      expect(isValidRoleCode('role:admin:user')).toBe(true)
      expect(isValidRoleCode('role-admin-user')).toBe(true)
      expect(isValidRoleCode('ROLE_ADMIN-USER:manager')).toBe(true)
    })
  })
})
