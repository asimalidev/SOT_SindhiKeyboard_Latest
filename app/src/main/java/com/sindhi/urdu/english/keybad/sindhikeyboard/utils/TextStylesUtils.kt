package com.sindhi.urdu.english.keybad.sindhikeyboard.utils

import kotlin.random.Random

class TextStylesUtils {
    companion object{
        public fun toItalic(text: String) = mapText(text, "abcdefghijklmnopqrstuvwxyz", "𝘢𝘣𝘤𝘥𝘦𝘧𝘨𝘩𝘪𝘫𝘬𝘭𝘮𝘯𝘰𝘱𝘲𝘳𝘴𝘵𝘶𝘷𝘸𝘹𝘺𝘻")

        public fun toFancySerif(text: String) = mapText(text, "abcdefghijklmnopqrstuvwxyz", "𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝓼𝔁𝔂𝔃")

        public fun toBubbleBold(text: String) = mapText(text.lowercase(), "abcdefghijklmnopqrstuvwxyz", "🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩")

        public fun toLightGrayCaps(text: String) = text.uppercase().map { "$it\u033F" }.joinToString("")

        public fun toBoldCaps(text: String) = mapText(text.lowercase(), "abcdefghijklmnopqrstuvwxyz", "𝗔𝗕𝗖𝗗𝗘𝗙𝗚𝗛𝗜𝗝𝗞𝗟𝗠𝗡𝗢𝗣𝗤𝗥𝗦𝗧𝗨𝗩𝗪𝗫𝗬𝗭")

        public fun toDotMatrix(text: String) = mapText(text.lowercase(), "abcdefghijklmnopqrstuvwxyz", "𝙖𝙗𝙘𝙙𝙚𝙛𝙜𝙝𝙞𝙟𝙠𝙡𝙢𝙣𝙤𝙥𝙦𝙧𝙨𝙩𝙪𝙫𝙬𝙭𝙮𝙯")

        public fun toInverted(text: String) = "⬛ $text ⬛"

        fun toBoldFancyStyle(input: String): String {
            val map = mapOf(
                'A' to 'ᗩ', 'B' to 'ᗷ', 'C' to 'ᑕ', 'D' to 'ᗪ', 'E' to 'E',
                'F' to 'ᖴ', 'G' to 'ᘜ', 'H' to 'ᕼ', 'I' to 'I', 'J' to 'ᒍ',
                'K' to 'ᖽ', 'L' to 'ᒪ', 'M' to 'ᗰ', 'N' to 'ᑎ', 'O' to 'O',
                'P' to 'ᑭ', 'Q' to 'ᑫ', 'R' to 'ᖇ', 'S' to 'S', 'T' to 'ᖶ',
                'U' to 'ᑌ', 'V' to 'ᐯ', 'W' to 'ᗯ', 'X' to '᙭', 'Y' to 'Y',
                'Z' to 'Z'
            )

            return input.uppercase().map { char ->
                map[char] ?: char
            }.joinToString("")
        }
        fun toScriptStyle(input: String): String {
            val map = mapOf(
                'a' to "𝒶", 'b' to "𝒷", 'c' to "𝒸", 'd' to "𝒹", 'e' to "𝑒",
                'f' to "𝒻", 'g' to "𝑔", 'h' to "𝒽", 'i' to "𝒾", 'j' to "𝒿",
                'k' to "𝓀", 'l' to "𝓁", 'm' to "𝓂", 'n' to "𝓃", 'o' to "𝑜",
                'p' to "𝓅", 'q' to "𝓆", 'r' to "𝓇", 's' to "𝓈", 't' to "𝓉",
                'u' to "𝓊", 'v' to "𝓋", 'w' to "𝓌", 'x' to "𝓍", 'y' to "𝓎",
                'z' to "𝓏",
                'A' to "𝒜", 'B' to "𝐵", 'C' to "𝒞", 'D' to "𝒟", 'E' to "𝐸",
                'F' to "𝐹", 'G' to "𝒢", 'H' to "𝐻", 'I' to "𝐼", 'J' to "𝐽",
                'K' to "𝒦", 'L' to "𝐿", 'M' to "𝑀", 'N' to "𝒩", 'O' to "𝒪",
                'P' to "𝒫", 'Q' to "𝒬", 'R' to "𝑅", 'S' to "𝒮", 'T' to "𝒯",
                'U' to "𝒰", 'V' to "𝒱", 'W' to "𝒲", 'X' to "𝒳", 'Y' to "𝒴",
                'Z' to "𝒵"
            )

            return buildString {
                input.forEach { char ->
                    append(map[char] ?: char)
                }
            }
        }

        fun glitchNepali(input: String): String {
            val combiningMarks = listOf(
                '\u0336', '\u0335', '\u0334', '\u0316', '\u0317', '\u0318',
                '\u0319', '\u031C', '\u031D', '\u031E', '\u031F', '\u0320',
                '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326',
                '\u0327', '\u0328', '\u0329', '\u032A', '\u032B', '\u032C'
            )
            return buildString {
                input.forEach { char ->
                    append(char)
                    // Randomly add 0-2 combining marks after each char
                    val count = (0..2).random()
                    repeat(count) {
                        append(combiningMarks.random())
                    }
                }
            }
        }
        fun addStrikethroughOverlay(input: String): String {
            val combiningLongStrokeOverlay = '\u0337'  // ̷
            return buildString {
                input.forEach { char ->
                    append(char)
                    append(combiningLongStrokeOverlay)
                }
            }
        }
        fun toUpsideDown(text: String): String {
            val map = mapOf(
                'a' to 'ɐ', 'b' to 'q', 'c' to 'ɔ', 'd' to 'p', 'e' to 'ǝ', 'f' to 'ɟ',
                'g' to 'ƃ', 'h' to 'ɥ', 'i' to 'ᴉ', 'j' to 'ɾ', 'k' to 'ʞ', 'l' to 'l',
                'm' to 'ɯ', 'n' to 'u', 'o' to 'o', 'p' to 'd', 'q' to 'b', 'r' to 'ɹ',
                's' to 's', 't' to 'ʇ', 'u' to 'n', 'v' to 'ʌ', 'w' to 'ʍ', 'x' to 'x',
                'y' to 'ʎ', 'z' to 'z',

                'A' to '∀', 'B' to "𐐒", 'C' to 'Ɔ', 'D' to '◖', 'E' to 'Ǝ', 'F' to 'Ⅎ',
                'G' to '⅁', 'H' to 'H', 'I' to 'I', 'J' to 'ſ', 'K' to 'ʞ', 'L' to '⅂',
                'M' to 'W', 'N' to 'N', 'O' to 'O', 'P' to 'Ԁ', 'Q' to 'Ό', 'R' to 'ᴚ',
                'S' to 'S', 'T' to '⊥', 'U' to '∩', 'V' to 'Λ', 'W' to 'M', 'X' to 'X',
                'Y' to '⅄', 'Z' to 'Z',

                '.' to '˙', ',' to "'", '\'' to ',', '"' to ",,", '?' to '¿', '!' to '¡',
                '[' to ']', ']' to '[', '(' to ')', ')' to '(', '{' to '}', '}' to '{',
                '<' to '>', '>' to '<', '&' to '⅋', '_' to '‾', ' ' to ' '
            )
            return text.reversed().map { map[it] ?: it }.joinToString("")
        }

        fun toCircledText(text: String): String {
            val normal = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val circled = arrayOf(
                "Ⓐ","Ⓑ","Ⓒ","Ⓓ","Ⓔ","Ⓕ","Ⓖ","Ⓗ","Ⓘ","Ⓙ","Ⓚ","Ⓛ","Ⓜ","Ⓝ","Ⓞ","Ⓟ","Ⓠ","Ⓡ","Ⓢ","Ⓣ","Ⓤ","Ⓥ","Ⓦ","Ⓧ","Ⓨ","Ⓩ",
                "ⓐ","ⓑ","ⓒ","ⓓ","ⓔ","ⓕ","ⓖ","ⓗ","ⓘ","ⓙ","ⓚ","ⓛ","ⓜ","ⓝ","ⓞ","ⓟ","ⓠ","ⓡ","ⓢ","ⓣ","ⓤ","ⓥ","ⓦ","ⓧ","ⓨ","ⓩ",
                "⓪","①","②","③","④","⑤","⑥","⑦","⑧","⑨"
            )

            return text.map { char ->
                val index = normal.indexOf(char)
                if (index != -1) circled[index] else char.toString()
            }.joinToString("")
        }
        public fun toSubscriptText(text: String): String {
            val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val subscript = mapOf(
                'a' to 'ₐ', 'b' to 'ᵦ', 'c' to "𝒸", 'd' to "𝒹", 'e' to 'ₑ', 'f' to "𝒻", 'g' to "𝓰",
                'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ',
                'o' to 'ₒ', 'p' to 'ₚ', 'q' to "𝓺", 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
                'v' to 'ᵥ', 'w' to "𝓌", 'x' to 'ₓ', 'y' to 'ᵧ', 'z' to "𝓏",
                'A' to 'ₐ', 'B' to 'ᵦ', 'C' to "𝒸", 'D' to "𝒹", 'E' to 'ₑ', 'F' to "𝒻", 'G' to "𝓰",
                'H' to 'ₕ', 'I' to 'ᵢ', 'J' to 'ⱼ', 'K' to 'ₖ', 'L' to 'ₗ', 'M' to 'ₘ', 'N' to 'ₙ',
                'O' to 'ₒ', 'P' to 'ₚ', 'Q' to "𝓺", 'R' to 'ᵣ', 'S' to 'ₛ', 'T' to 'ₜ', 'U' to 'ᵤ',
                'V' to 'ᵥ', 'W' to "𝓌", 'X' to 'ₓ', 'Y' to 'ᵧ', 'Z' to "𝓏",
                '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
                '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉'
            )

            return text.map { subscript[it] ?: it }.joinToString("")
        }
        fun toItalicStrikethrough(text: String): String {
            val italicMap = mapOf(
                "a" to "𝑎", "b" to "𝑏", "c" to "𝑐", "d" to "𝑑", "e" to "𝑒", "f" to "𝑓", "g" to "𝑔",
                "h" to "ℎ", "i" to "𝑖", "j" to "𝑗", "k" to "𝑘", "l" to "𝑙", "m" to "𝑚", "n" to "𝑛",
                "o" to "𝑜", "p" to "𝑝", "q" to "𝑞", "r" to "𝑟", "s" to "𝑠", "t" to "𝑡", "u" to "𝑢",
                "v" to "𝑣", "w" to "𝑤", "x" to "𝑥", "y" to "𝑦", "z" to "𝑧",

                "A" to "𝐴", "B" to "𝐵", "C" to "𝐶", "D" to "𝐷", "E" to "𝐸", "F" to "𝐹", "G" to "𝐺",
                "H" to "𝐻", "I" to "𝐼", "J" to "𝐽", "K" to "𝐾", "L" to "𝐿", "M" to "𝑀", "N" to "𝑁",
                "O" to "𝑂", "P" to "𝑃", "Q" to "𝑄", "R" to "𝑅", "S" to "𝑆", "T" to "𝑇", "U" to "𝑈",
                "V" to "𝑉", "W" to "𝑊", "X" to "𝑋", "Y" to "𝑌", "Z" to "𝑍"
            )

            val combiningStrikethrough = '\u0336'

            return text.map { char ->
                val key = char.toString()
                val italicChar = italicMap[key] ?: key
                "$italicChar$combiningStrikethrough"
            }.joinToString("")
        }

        fun toBoldFraktur(text: String): String {
            val map = mapOf(
                "a" to "𝖆", "b" to "𝖇", "c" to "𝖈", "d" to "𝖉", "e" to "𝖊", "f" to "𝖋", "g" to "𝖌",
                "h" to "𝖍", "i" to "𝖎", "j" to "𝖏", "k" to "𝖐", "l" to "𝖑", "m" to "𝖒", "n" to "𝖓",
                "o" to "𝖔", "p" to "𝖕", "q" to "𝖖", "r" to "𝖗", "s" to "𝖘", "t" to "𝖙", "u" to "𝖚",
                "v" to "𝖛", "w" to "𝖜", "x" to "𝖝", "y" to "𝖞", "z" to "𝖟",
                "A" to "𝕬", "B" to "𝕭", "C" to "𝕮", "D" to "𝕯", "E" to "𝕰", "F" to "𝕱", "G" to "𝕲",
                "H" to "𝕳", "I" to "𝕴", "J" to "𝕵", "K" to "𝕶", "L" to "𝕷", "M" to "𝕸", "N" to "𝕹",
                "O" to "𝕺", "P" to "𝕻", "Q" to "𝕼", "R" to "𝕽", "S" to "𝕾", "T" to "𝕿", "U" to "𝖀",
                "V" to "𝖁", "W" to "𝖂", "X" to "𝖃", "Y" to "𝖄", "Z" to "𝖅"
            )

            return text.map { char -> map[char.toString()] ?: char.toString() }.joinToString("")
        }
        fun toDoubleStruck1(text: String): String {
            val map = mapOf(
                "a" to "𝕒", "b" to "𝕓", "c" to "𝕔", "d" to "𝕕", "e" to "𝕖", "f" to "𝕗", "g" to "𝕘",
                "h" to "𝕙", "i" to "𝕚", "j" to "𝕛", "k" to "𝕜", "l" to "𝕝", "m" to "𝕞", "n" to "𝕟",
                "o" to "𝕠", "p" to "𝕡", "q" to "𝕢", "r" to "𝕣", "s" to "𝕤", "t" to "𝕥", "u" to "𝕦",
                "v" to "𝕧", "w" to "𝕨", "x" to "𝕩", "y" to "𝕪", "z" to "𝕫",
                "A" to "𝔸", "B" to "𝔹", "C" to "ℂ", "D" to "𝔻", "E" to "𝔼", "F" to "𝔽", "G" to "𝔾",
                "H" to "ℍ", "I" to "𝕀", "J" to "𝕁", "K" to "𝕂", "L" to "𝕃", "M" to "𝕄", "N" to "ℕ",
                "O" to "𝕆", "P" to "ℙ", "Q" to "ℚ", "R" to "ℝ", "S" to "𝕊", "T" to "𝕋", "U" to "𝕌",
                "V" to "𝕍", "W" to "𝕎", "X" to "𝕏", "Y" to "𝕐", "Z" to "ℤ"
            )
            return text.map { map[it.toString()] ?: it.toString() }.joinToString("")
        }
        fun toSansSerifItalic(text: String): String {
            val map = mapOf(
                "a" to "𝘢", "b" to "𝘣", "c" to "𝘤", "d" to "𝘥", "e" to "𝘦", "f" to "𝘧", "g" to "𝘨",
                "h" to "𝘩", "i" to "𝘪", "j" to "𝘫", "k" to "𝘬", "l" to "𝘭", "m" to "𝘮", "n" to "𝘯",
                "o" to "𝘰", "p" to "𝘱", "q" to "𝘲", "r" to "𝘳", "s" to "𝘴", "t" to "𝘵", "u" to "𝘶",
                "v" to "𝘷", "w" to "𝘸", "x" to "𝘹", "y" to "𝘺", "z" to "𝘻",
                "A" to "𝘈", "B" to "𝘉", "C" to "𝘊", "D" to "𝘋", "E" to "𝘌", "F" to "𝘍", "G" to "𝘎",
                "H" to "𝘏", "I" to "𝘐", "J" to "𝘑", "K" to "𝘒", "L" to "𝘓", "M" to "𝘔", "N" to "𝘕",
                "O" to "𝘖", "P" to "𝘗", "Q" to "𝘘", "R" to "𝘙", "S" to "𝘚", "T" to "𝘛", "U" to "𝘜",
                "V" to "𝘝", "W" to "𝘞", "X" to "𝘟", "Y" to "𝘠", "Z" to "𝘡"
            )
            return text.map { map[it.toString()] ?: it.toString() }.joinToString("")
        }

        fun toBoldScript(text: String): String {
            val map = mapOf(
                "a" to "𝓪", "b" to "𝓫", "c" to "𝓬", "d" to "𝓭", "e" to "𝓮", "f" to "𝓯", "g" to "𝓰",
                "h" to "𝓱", "i" to "𝓲", "j" to "𝓳", "k" to "𝓴", "l" to "𝓵", "m" to "𝓶", "n" to "𝓷",
                "o" to "𝓸", "p" to "𝓹", "q" to "𝓺", "r" to "𝓻", "s" to "𝓼", "t" to "𝓽", "u" to "𝓾",
                "v" to "𝓿", "w" to "𝔀", "x" to "𝔁", "y" to "𝔂", "z" to "𝔃",
                "A" to "𝓐", "B" to "𝓑", "C" to "𝓒", "D" to "𝓓", "E" to "𝓔", "F" to "𝓕", "G" to "𝓖",
                "H" to "𝓗", "I" to "𝓘", "J" to "𝓙", "K" to "𝓚", "L" to "𝓛", "M" to "𝓜", "N" to "𝓝",
                "O" to "𝓞", "P" to "𝓟", "Q" to "𝓠", "R" to "𝓡", "S" to "𝓢", "T" to "𝓣", "U" to "𝓤",
                "V" to "𝓥", "W" to "𝓦", "X" to "𝓧", "Y" to "𝓨", "Z" to "𝓩"
            )
            return text.map { map[it.toString()] ?: it.toString() }.joinToString("")
        }
        fun toFraktur(text: String): String {
            val map = mapOf(
                "a" to "𝔞", "b" to "𝔟", "c" to "𝔠", "d" to "𝔡", "e" to "𝔢", "f" to "𝔣", "g" to "𝔤",
                "h" to "𝔥", "i" to "𝔦", "j" to "𝔧", "k" to "𝔨", "l" to "𝔩", "m" to "𝔪", "n" to "𝔫",
                "o" to "𝔬", "p" to "𝔭", "q" to "𝔮", "r" to "𝔯", "s" to "𝔰", "t" to "𝔱", "u" to "𝔲",
                "v" to "𝔳", "w" to "𝔴", "x" to "𝔵", "y" to "𝔶", "z" to "𝔷",
                "A" to "𝔄", "B" to "𝔅", "C" to "ℭ", "D" to "𝔇", "E" to "𝔈", "F" to "𝔉", "G" to "𝔊",
                "H" to "ℌ", "I" to "ℑ", "J" to "𝔍", "K" to "𝔎", "L" to "𝔏", "M" to "𝔐", "N" to "𝔑",
                "O" to "𝔒", "P" to "𝔓", "Q" to "𝔔", "R" to "ℜ", "S" to "𝔖", "T" to "𝔗", "U" to "𝔘",
                "V" to "𝔙", "W" to "𝔚", "X" to "𝔛", "Y" to "𝔜", "Z" to "ℨ"
            )
            return text.map { map[it.toString()] ?: it.toString() }.joinToString("")
        }

        public fun toDoubleStruck(text: String): String {
            val map = mapOf(
                "a" to "𝕒", "b" to "𝕓", "c" to "𝕔", "d" to "𝕕", "e" to "𝕖", "f" to "𝕗", "g" to "𝕘",
                "h" to "𝕙", "i" to "𝕚", "j" to "𝕛", "k" to "𝕜", "l" to "𝕝", "m" to "𝕞", "n" to "𝕟",
                "o" to "𝕠", "p" to "𝕡", "q" to "𝕢", "r" to "𝕣", "s" to "𝕤", "t" to "𝕥", "u" to "𝕦",
                "v" to "𝕧", "w" to "𝕨", "x" to "𝕩", "y" to "𝕪", "z" to "𝕫",
                "A" to "𝔸", "B" to "𝔹", "C" to "ℂ", "D" to "𝔻", "E" to "𝔼", "F" to "𝔽", "G" to "𝔾",
                "H" to "ℍ", "I" to "𝕀", "J" to "𝕁", "K" to "𝕂", "L" to "𝕃", "M" to "𝕄", "N" to "ℕ",
                "O" to "𝕆", "P" to "ℙ", "Q" to "ℚ", "R" to "ℝ", "S" to "𝕊", "T" to "𝕋", "U" to "𝕌",
                "V" to "𝕍", "W" to "𝕎", "X" to "𝕏", "Y" to "𝕐", "Z" to "ℤ"
            )
            return text.map { map[it.toString()] ?: it.toString() }.joinToString("")
        }

        public fun toBlackBoxedText(text: String): String {
            val map = mapOf(
                'A' to "🅐", 'B' to "🅑", 'C' to "🅒", 'D' to "🅓", 'E' to "🅔", 'F' to "🅕", 'G' to "🅖",
                'H' to "🅗", 'I' to "🅘", 'J' to "🅙", 'K' to "🅚", 'L' to "🅛", 'M' to "🅜", 'N' to "🅝",
                'O' to "🅞", 'P' to "🅟", 'Q' to "🅠", 'R' to "🅡", 'S' to "🅢", 'T' to "🅣", 'U' to "🅤",
                'V' to "🅥", 'W' to "🅦", 'X' to "🅧", 'Y' to "🅨", 'Z' to "🅩",
                'a' to "🅐", 'b' to "🅑", 'c' to "🅒", 'd' to "🅓", 'e' to "🅔", 'f' to "🅕", 'g' to "🅖",
                'h' to "🅗", 'i' to "🅘", 'j' to "🅙", 'k' to "🅚", 'l' to "🅛", 'm' to "🅜", 'n' to "🅝",
                'o' to "🅞", 'p' to "🅟", 'q' to "🅠", 'r' to "🅡", 's' to "🅢", 't' to "🅣", 'u' to "🅤",
                'v' to "🅥", 'w' to "🅦", 'x' to "🅧", 'y' to "🅨", 'z' to "🅩"
            )

            return text.map { map[it] ?: it.toString() }.joinToString("")
        }
        public fun toFancyAltText(text: String): String {
            val map = mapOf(
                'a' to 'α', 'b' to 'в', 'c' to '¢', 'd' to '∂', 'e' to 'є', 'f' to 'ƒ', 'g' to 'g',
                'h' to 'н', 'i' to 'ι', 'j' to 'נ', 'k' to 'к', 'l' to 'ℓ', 'm' to 'м', 'n' to 'η',
                'o' to 'σ', 'p' to 'ρ', 'q' to 'q', 'r' to 'я', 's' to 'ѕ', 't' to 'т', 'u' to 'υ',
                'v' to 'ν', 'w' to 'ω', 'x' to 'χ', 'y' to 'у', 'z' to 'z',
                'A' to 'Λ', 'B' to 'β', 'C' to 'C', 'D' to 'D', 'E' to 'Σ', 'F' to 'F', 'G' to 'G',
                'H' to 'H', 'I' to 'Ι', 'J' to 'J', 'K' to 'Κ', 'L' to 'L', 'M' to 'M', 'N' to 'П',
                'O' to 'Θ', 'P' to 'Ρ', 'Q' to 'Q', 'R' to 'R', 'S' to 'S', 'T' to 'T', 'U' to 'Ц',
                'V' to 'V', 'W' to 'Ш', 'X' to 'Ж', 'Y' to 'Ψ', 'Z' to 'Z'
            )
            return text.map { map[it] ?: it }.joinToString("")
        }

        public fun toCursiveScriptText(text: String): String {
            val map = mapOf(
                'a' to "𝓪", 'b' to "𝓫", 'c' to "𝓬", 'd' to "𝓭", 'e' to "𝓮", 'f' to "𝓯", 'g' to "𝓰",
                'h' to "𝓱", 'i' to "𝓲", 'j' to "𝓳", 'k' to "𝓴", 'l' to "𝓵", 'm' to "𝓶", 'n' to "𝓷",
                'o' to "𝓸", 'p' to "𝓹", 'q' to "𝓺", 'r' to "𝓻", 's' to "𝓼", 't' to "𝓽", 'u' to "𝓾",
                'v' to "𝓿", 'w' to "𝔀", 'x' to "𝔁", 'y' to "𝔂", 'z' to "𝔃",
                'A' to "𝓐", 'B' to "𝓑", 'C' to "𝓒", 'D' to "𝓓", 'E' to "𝓔", 'F' to "𝓕", 'G' to "𝓖",
                'H' to "𝓗", 'I' to "𝓘", 'J' to "𝓙", 'K' to "𝓚", 'L' to "𝓛", 'M' to "𝓜", 'N' to "𝓝",
                'O' to "𝓞", 'P' to "𝓟", 'Q' to "𝓠", 'R' to "𝓡", 'S' to "𝓢", 'T' to "𝓣", 'U' to "𝓤",
                'V' to "𝓥", 'W' to "𝓦", 'X' to "𝓧", 'Y' to "𝓨", 'Z' to "𝓩"
            )

            return text.map { map[it] ?: it.toString() }.joinToString("")
        }
        public fun toEnclosedAlphanumerics(text: String): String {
            val map = mapOf(
                'A' to "🄰", 'B' to "🄱", 'C' to "🄲", 'D' to "🄳", 'E' to "🄴", 'F' to "🄵",
                'G' to "🄶", 'H' to "🄷", 'I' to "🄸", 'J' to "🄹", 'K' to "🄺", 'L' to "🄻",
                'M' to "🄼", 'N' to "🄽", 'O' to "🄾", 'P' to "🄿", 'Q' to "🅀", 'R' to "🅁",
                'S' to "🅂", 'T' to "🅃", 'U' to "🅄", 'V' to "🅅", 'W' to "🅆", 'X' to "🅇",
                'Y' to "🅈", 'Z' to "🅉",
                'a' to "🄰", 'b' to "🄱", 'c' to "🄲", 'd' to "🄳", 'e' to "🄴", 'f' to "🄵",
                'g' to "🄶", 'h' to "🄷", 'i' to "🄸", 'j' to "🄹", 'k' to "🄺", 'l' to "🄻",
                'm' to "🄼", 'n' to "🄽", 'o' to "🄾", 'p' to "🄿", 'q' to "🅀", 'r' to "🅁",
                's' to "🅂", 't' to "🅃", 'u' to "🅄", 'v' to "🅅", 'w' to "🅆", 'x' to "🅇",
                'y' to "🅈", 'z' to "🅉"
            )
            return text.map { map[it] ?: it.toString() }.joinToString("")
        }

        fun getCustomDecoratedText(text: String): String {
            val prefix = "∙∙·▫▫ᵒᴼᵒ▫ₒₒ▫ᵒᴼ"
            val suffix = "ᴼᵒ▫ₒₒ▫ᵒᴼᵒ▫▫·∙∙"
            val circledText = toCircled(text)
            return prefix + circledText + suffix
        }
        fun toCircled(text: String): String {
            val map = mapOf(
                'a' to 'ⓐ', 'b' to 'ⓑ', 'c' to 'ⓒ', 'd' to 'ⓓ', 'e' to 'ⓔ', 'f' to 'ⓕ', 'g' to 'ⓖ',
                'h' to 'ⓗ', 'i' to 'ⓘ', 'j' to 'ⓙ', 'k' to 'ⓚ', 'l' to 'ⓛ', 'm' to 'ⓜ', 'n' to 'ⓝ',
                'o' to 'ⓞ', 'p' to 'ⓟ', 'q' to 'ⓠ', 'r' to 'ⓡ', 's' to 'ⓢ', 't' to 'ⓣ', 'u' to 'ⓤ',
                'v' to 'ⓥ', 'w' to 'ⓦ', 'x' to 'ⓧ', 'y' to 'ⓨ', 'z' to 'ⓩ',
                'A' to 'Ⓐ', 'B' to 'Ⓑ', 'C' to 'Ⓒ', 'D' to 'Ⓓ', 'E' to 'Ⓔ', 'F' to 'Ⓕ', 'G' to 'Ⓖ',
                'H' to 'Ⓗ', 'I' to 'Ⓘ', 'J' to 'Ⓙ', 'K' to 'Ⓚ', 'L' to 'Ⓛ', 'M' to 'Ⓜ', 'N' to 'Ⓝ',
                'O' to 'Ⓞ', 'P' to 'Ⓟ', 'Q' to 'Ⓠ', 'R' to 'Ⓡ', 'S' to 'Ⓢ', 'T' to 'Ⓣ', 'U' to 'Ⓤ',
                'V' to 'Ⓥ', 'W' to 'Ⓦ', 'X' to 'Ⓧ', 'Y' to 'Ⓨ', 'Z' to 'Ⓩ',
                ' ' to ' '
            )
            return text.map { map[it] ?: it }.joinToString("")
        }
        fun toFancyStyle(input: String): String {
            val fancyMap = mapOf(
                'a' to 'ꪖ', 'b' to '᥇', 'c' to 'ᥴ', 'd' to 'ᦔ', 'e' to 'ꫀ',
                'f' to 'ᠻ', 'g' to 'ᧁ', 'h' to 'ꫝ', 'i' to '꠸', 'j' to '᭙',
                'k' to '᥅', 'l' to 'ꪶ', 'm' to 'ᥴ', 'n' to 'ꪀ', 'o' to 'ꪮ',
                'p' to '᭙', 'q' to 'ᥘ', 'r' to 'ꪜ', 's' to 'ꪑ', 't' to 'ᥴ',
                'u' to 'ꪊ', 'v' to 'ꪜ', 'w' to '᭙', 'x' to 'ᥑ', 'y' to 'ꪗ', 'z' to 'ꪓ',
                ' ' to ' '
            )

            return input.lowercase().map { char ->
                val fancyChar = fancyMap[char] ?: char
                if (char == ' ') " " else "\"$fancyChar\""
            }.joinToString(" ")
        }

        public fun toUnderlineOverlayStyle(text: String): String {
            val overlay = '\u033D' // Combining tilde overlay
            val underline = '\u0332' // Combining low line (underline)

            return text.map {
                if (it != ' ') "$it$overlay$underline"
                else " "
            }.joinToString("")
        }
        fun toUsamaStyle(input: String): String {
            val fancyMap = mapOf(
                'a' to 'ǟ', 'b' to 'ɮ', 'c' to 'ƈ', 'd' to 'ɖ', 'e' to 'ɛ',
                'f' to 'ʄ', 'g' to 'ɢ', 'h' to 'ɦ', 'i' to 'ɨ', 'j' to 'ʝ',
                'k' to 'ʞ', 'l' to 'ʟ', 'm' to 'ʍ', 'n' to 'ռ', 'o' to 'օ',
                'p' to 'ք', 'q' to 'զ', 'r' to 'ʀ', 's' to 'ֆ', 't' to 'ȶ',
                'u' to 'ʊ', 'v' to 'ѵ', 'w' to 'ա', 'x' to 'ҳ', 'y' to 'ʏ', 'z' to 'ʐ',
                ' ' to ' '
            )

            val styled = input.lowercase().map { char ->
                fancyMap[char] ?: char
            }.joinToString("")

            return "ᘛᗽ $styled ᘀᘗ"
        }

        public fun toZalgoText(input: String): String {
            val zalgoUp = listOf(
                '\u030d', '\u030e', '\u0304', '\u0305', '\u033f', '\u0311', '\u0306', '\u0310',
                '\u0352', '\u0357', '\u0351', '\u0307', '\u0308', '\u030a', '\u0342', '\u0343',
                '\u0344', '\u034a', '\u034b', '\u034c', '\u0303', '\u0302', '\u030c', '\u0350',
                '\u0300', '\u0301', '\u030b', '\u030f', '\u0312', '\u0313', '\u0314', '\u033d',
                '\u0309', '\u0363', '\u0364', '\u0365', '\u0366', '\u0367', '\u0368', '\u0369',
                '\u036a', '\u036b', '\u036c', '\u036d', '\u036e', '\u036f', '\u033e', '\u035b',
                '\u0346', '\u031a'
            )

            val zalgoDown = listOf(
                '\u0316', '\u0317', '\u0318', '\u0319', '\u031c', '\u031d', '\u031e', '\u031f',
                '\u0320', '\u0324', '\u0325', '\u0326', '\u0329', '\u032a', '\u032b', '\u032c',
                '\u032d', '\u032e', '\u032f', '\u0330', '\u0331', '\u0332', '\u0333', '\u0339',
                '\u033a', '\u033b', '\u033c', '\u0345', '\u0347', '\u0348', '\u0349', '\u034d',
                '\u034e', '\u0353', '\u0354', '\u0355', '\u0356', '\u0359', '\u035a', '\u0323'
            )

            val maxUp = 5
            val maxDown = 5

            val result = StringBuilder()
            for (char in input) {
                if (char.isWhitespace()) {
                    result.append(char)
                    continue
                }
                result.append(char)

                repeat(Random.nextInt(1, maxUp + 1)) {
                    result.append(zalgoUp.random())
                }

                repeat(Random.nextInt(1, maxDown + 1)) {
                    result.append(zalgoDown.random())
                }
            }

            return result.toString()
        }
        public fun toCherokeeStyle(input: String): String {
            val map = mapOf(
                'a' to 'Ꭷ',
                'b' to 'Ᏼ',
                'c' to 'ፈ',
                'd' to 'Ꮧ',
                'e' to 'Ꮄ',
                'f' to 'Ꭶ',
                'g' to 'Ꮆ',
                'h' to 'Ꮒ',
                'i' to 'Ꭵ',
                'j' to 'Ꮰ',
                'k' to 'Ꭷ',
                'l' to 'Ꮭ',
                'm' to 'Ꮇ',
                'n' to 'Ꮑ',
                'o' to 'Ꭷ',
                'p' to 'Ꮲ',
                'q' to 'Ꮛ',
                'r' to 'Ꮅ',
                's' to 'Ꮝ',
                't' to 'Ꮦ',
                'u' to 'Ꭴ',
                'v' to 'Ꮩ',
                'w' to 'Ꮗ',
                'x' to 'Ꮖ',
                'y' to 'Ꮿ',
                'z' to 'Ꮓ',
                ' ' to ' '
            )

            return input.lowercase().map { char ->
                map[char] ?: char
            }.joinToString("")
        }
        public  fun wrapWithMarkers(input: String): String {
            return input.map { char ->
                if (char == ' ') " " else "⧼$char⧽"
            }.joinToString("")
        }
        public fun decorateText(input: String): String {
            val prefix = "°⨳°·..·°⨳°⊹٭ "
            val suffix = " ٭⊹°⨳°·..·°⨳°"
            return prefix + input + suffix
        }
        public fun decorateTextB(input: String): String {
            val prefix = "°•.•°¤*✬.•°°• "
            val suffix = " °•°•.✬*¤°•.•°"
            return prefix + input + suffix
        }
        public fun stylishDecorate(input: String): String {
            val prefix = ",¡i|¹i¡¡i¹|i¡, "
            val suffix = " ,¡i|¹i¡¡i¹|i¡,"

            // Function to convert normal text to bold italic (Mathematical Sans-Serif Bold Italic Unicode)
            fun toBoldItalic(text: String): String {
                val offsetUpper = 0x1D63C - 'A'.code  // Unicode offset for uppercase A
                val offsetLower = 0x1D656 - 'a'.code  // Unicode offset for lowercase a

                return text.map { ch ->
                    when {
                        ch in 'A'..'Z' -> (ch.code + offsetUpper).toChar()
                        ch in 'a'..'z' -> (ch.code + offsetLower).toChar()
                        ch == ' ' -> ' '  // keep space as is
                        else -> ch       // other chars unchanged
                    }
                }.joinToString("")
            }

            return prefix + toBoldItalic(input) + suffix
        }
        public fun decorateWithFace(input: String): String {
            val face = "(◉‿◉)"
            return "$face $input $face"
        }
        public fun fancyDecoratedText(input: String): String {
            // Replace certain letters with emojis inside the text
            val emojiMap = mapOf(
                'l' to "l💍",  // after first 'l' add ring emoji
                'c' to "c🍑"   // after 'c' add peach emoji
            )

            val decoratedText = input.map { char ->
                emojiMap[char] ?: char.toString()
            }.joinToString("")

            return "᯽ $decoratedText ᯽ 🍎 ⋆ 🍬 ⋆ 🎂"
        }
        public fun heartStyle(input: String): String {
            return input.map {
                if (it == ' ') '♥' else it
            }.joinToString("♥")
        }

        public  fun mapText(text: String, source: String, target: String): String {
            val map = source.zip(target).toMap()
            return text.map { map[it] ?: it }.joinToString("")
        }

    }
}