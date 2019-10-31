# 문자열에서 핸드폰 번호 뒷자리인 숫자 4개를 ####로 바꾸는 코드
import re

s = """
park 010-9999-9988
kim 010-9909-7789
lee 010-8789-7768
"""

pat = re.compile("(\d{3}[-]\d{4})[-]\d{4}")
result = pat.sub("\g<1>-####", s)
print(result)