#!/home/ttolmany/scripts/tide-alert/venv/bin/python3
"""Hojak Portal 안드로이드 런처 아이콘 생성 스크립트.
디자인: 다크 네이비 원형 림 뱃지 + 골드 피시 + 선박 닻(Anchor) + 만년필 펜 + 입체 섀도우.
"""
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

SIZE = 1024
RES_DIR = Path("/home/ttolmany/android-projects/HojakPortal/app/src/main/res")

BG_COLOR = (18, 22, 28)        # #12161c
BG_INNER = (26, 33, 43)        # #1a212b
RIM_COLOR = (46, 56, 70)       # #2e3846
ACCENT_GOLD = (245, 166, 35)   # #f5a623
ACCENT_ORANGE = (255, 122, 61) # #ff7a3d
SEA_BLUE = (56, 189, 248)      # #38bdf8
WHITE = (255, 255, 255)
DARK_PEN = (220, 228, 238)

def draw_master_icon():
    scale = 2
    W = SIZE * scale
    H = SIZE * scale
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    cx, cy = W // 2, H // 2
    r_badge = int(W * 0.44)

    # 1. 원형 림 뱃지 본체
    draw.ellipse(
        [cx - r_badge, cy - r_badge, cx + r_badge, cy + r_badge],
        fill=BG_INNER + (255,),
        outline=RIM_COLOR + (255,),
        width=12 * scale
    )

    # 그림자 레이어
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)

    # 2. 하단 파도 (Sea Waves)
    wave_y1 = int(H * 0.67)
    wave_y2 = int(H * 0.73)

    # 3. 중앙 닻 (Anchor) 실루엣
    anc_x = cx
    anc_y = int(H * 0.52)
    s_anc = 1.35 * scale

    # 닻 그림자
    sdraw.ellipse([anc_x - 30 * s_anc + 10 * scale, anc_y - 70 * s_anc + 18 * scale, anc_x + 30 * s_anc + 10 * scale, anc_y - 20 * s_anc + 18 * scale], fill=(0, 0, 0, 130))
    sdraw.rectangle([anc_x - 12 * s_anc + 10 * scale, anc_y - 30 * s_anc + 18 * scale, anc_x + 12 * s_anc + 10 * scale, anc_y + 80 * s_anc + 18 * scale], fill=(0, 0, 0, 130))
    shadow = shadow.filter(ImageFilter.GaussianBlur(14 * scale))
    img = Image.alpha_composite(img, shadow)
    draw = ImageDraw.Draw(img)

    # 닻(Anchor) 본체
    # 상단 링
    draw.ellipse([anc_x - 30 * s_anc, anc_y - 75 * s_anc, anc_x + 30 * s_anc, anc_y - 25 * s_anc], outline=WHITE + (255,), width=int(11 * s_anc))
    # 기둥
    draw.line([(anc_x, anc_y - 35 * s_anc), (anc_x, anc_y + 80 * s_anc)], fill=WHITE + (255,), width=int(14 * s_anc))
    # 가로대
    draw.line([(anc_x - 55 * s_anc, anc_y - 5 * s_anc), (anc_x + 55 * s_anc, anc_y - 5 * s_anc)], fill=WHITE + (255,), width=int(12 * s_anc))
    # 갈고리 원호
    draw.arc([anc_x - 75 * s_anc, anc_y + 15 * s_anc, anc_x + 75 * s_anc, anc_y + 95 * s_anc], start=25, end=155, fill=WHITE + (255,), width=int(14 * s_anc))

    # 4. 황금 물고기 심볼 (Gold Fish - 닻 중앙에 유영)
    fx = cx + 25 * scale
    fy = cy - 20 * scale
    fish_pts = [
        (fx + 140 * scale, fy - 10 * scale),  # 입
        (fx + 95 * scale, fy - 70 * scale),   # 등 앞
        (fx + 10 * scale, fy - 85 * scale),   # 등 최고점
        (fx - 70 * scale, fy - 55 * scale),   # 등 뒤
        (fx - 130 * scale, fy - 20 * scale),  # 꼬리 목
        (fx - 185 * scale, fy - 55 * scale),  # 꼬리 위
        (fx - 165 * scale, fy),               # 꼬리 홈
        (fx - 185 * scale, fy + 55 * scale),  # 꼬리 아래
        (fx - 130 * scale, fy + 20 * scale),  # 꼬리 목 아래
        (fx - 65 * scale, fy + 55 * scale),   # 배 뒤
        (fx + 15 * scale, fy + 70 * scale),   # 배 최저점
        (fx + 95 * scale, fy + 45 * scale),   # 배 앞
    ]
    # 물고기 그림자
    f_sh = [(p[0] + 8 * scale, p[1] + 12 * scale) for p in fish_pts]
    draw.polygon(f_sh, fill=(10, 14, 20, 180))
    # 물고기 본체
    draw.polygon(fish_pts, fill=ACCENT_GOLD + (255,), outline=ACCENT_ORANGE + (255,), width=4 * scale)

    # 등지느러미
    dorsal = [
        (fx - 10 * scale, fy - 85 * scale),
        (fx - 30 * scale, fy - 115 * scale),
        (fx - 65 * scale, fy - 65 * scale),
    ]
    draw.polygon(dorsal, fill=ACCENT_ORANGE + (255,))

    # 눈
    eye_x = fx + 105 * scale
    eye_y = fy - 12 * scale
    draw.ellipse([eye_x - 8 * scale, eye_y - 8 * scale, eye_x + 8 * scale, eye_y + 8 * scale], fill=WHITE + (255,))
    draw.ellipse([eye_x - 4 * scale, eye_y - 4 * scale, eye_x + 4 * scale, eye_y + 4 * scale], fill=(20, 25, 35, 255))

    # 5. 만년필 펜 (대각선으로 교차하여 기록 상징)
    pen_x = cx - 120 * scale
    pen_y = cy - 80 * scale
    angle = -math.radians(45)
    pen_len = int(220 * scale)
    pen_w = 20 * scale

    def pen_pt(l, w):
        rx = pen_x + l * math.cos(angle) - w * math.sin(angle)
        ry = pen_y + l * math.sin(angle) + w * math.cos(angle)
        return (rx, ry)

    nib = pen_pt(0, 0)
    b1 = pen_pt(35 * scale, -pen_w // 2)
    b2 = pen_pt(pen_len, -pen_w // 2)
    b3 = pen_pt(pen_len, pen_w // 2)
    b4 = pen_pt(35 * scale, pen_w // 2)

    draw.polygon([nib, b1, b2, b3, b4], fill=DARK_PEN + (255,), outline=WHITE + (255,), width=3 * scale)
    # 펜 닙 골드 팁
    draw.polygon([nib, b1, b4], fill=ACCENT_GOLD + (255,))

    # 6. 파도 라인 (하단)
    wave1_pts = []
    for x in range(int(W * 0.16), int(W * 0.84), 4 * scale):
        y = wave_y1 + math.sin((x - cx) * 0.009) * 16 * scale + math.cos((x - cx) * 0.018) * 8 * scale
        wave1_pts.append((x, y))
    for i in range(len(wave1_pts) - 1):
        draw.line([wave1_pts[i], wave1_pts[i + 1]], fill=SEA_BLUE + (230,), width=7 * scale)

    wave2_pts = []
    for x in range(int(W * 0.22), int(W * 0.78), 4 * scale):
        y = wave_y2 + math.sin((x - cx) * 0.012 + 1.4) * 14 * scale
        wave2_pts.append((x, y))
    for i in range(len(wave2_pts) - 1):
        draw.line([wave2_pts[i], wave2_pts[i + 1]], fill=SEA_BLUE + (180,), width=5 * scale)

    # 7. 안드로이드 리소스 규격별 리사이즈 및 저장
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, sz in sizes.items():
        out_dir = RES_DIR / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        resized = img.resize((sz, sz), Image.Resampling.LANCZOS)
        resized.save(out_dir / "ic_launcher.png", "PNG")
        resized.save(out_dir / "ic_launcher_round.png", "PNG")
        print(f"[icon] wrote {out_dir}/ic_launcher.png ({sz}x{sz})")

    # 512px 웹용 아이콘 저장
    portal_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
    portal_512.save(RES_DIR.parent / "ic_launcher-web.png", "PNG")
    print(f"[icon] wrote 512px launcher icon")

if __name__ == "__main__":
    draw_master_icon()
