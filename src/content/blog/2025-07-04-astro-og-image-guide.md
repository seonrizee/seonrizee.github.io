---
title: "Astro 블로그에 썸네일(og:image) 추가하기"
summary: "Astro 블로그에 썸네일(og:image) 추가하기"
date: "July 04 2025"
draft: false
tags:
- Frontend
---

## Astro 블로그에 썸네일(og:image) 추가하기

이 글은 Astro를 사용하여 개발한 블로그에서 각 포스트마다 고유한 썸네일을 설정하고, 없을 경우엔 기본 이미지가 나오도록 하는 과정에서 겪었던 문제와 해결 방법을 정리한 글입니다.

---

### 문제 상황: 왜 이 사진이 뜰까?

Astro를 이용하여 블로그를 만든 뒤에 새로운 글을 작성하면 종종 가까운 지인에게 전달하여 평가를 부탁하고 있습니다. 이 때 공유를 하면 나타나는 미리보기 화면에서, 제 프로필 이미지가 아닌 블로그를 만들 때 사용한 테마인 [`Astro-Sphere`](https://github.com/markhorn-dev/astro-sphere)의 기본 이미지만 반복해서 나오는 것이 아쉬웠습니다. 

![Astro Sphere 이미지가 미리보기에 표현 됨](./img/2025-07-04-image.png)

Astro Sphere 이미지가 미리보기에 표현 됨

### 1. Astro와 Open Graph

소셜 미디어에 링크를 공유할 때 표시되는 **미리보기**(제목, 설명, 이미지)는 메타가 페이스북이던 시절에 개발한 [**오픈그래프(Open Graph) 프로토콜**](https://ogp.me/)에 의해 제어됩니다. 그중 이미지를 담당하는 것이 바로 `<meta property="og:image" content="이미지_URL">` 태그입니다.

Astro에서는 이 과정을 레이아웃과 Props를 통해 동적으로 처리할 수 있습니다. 

1. 각 페이지(`.astro`)의 프론트매터(Frontmatter)에서 `title`, `description`, `image` 등의 정보를 정의한다.
2. 이 정보들을 공통 레이아웃(`.astro`)에 `props`로 전달한다. 
3. 레이아웃은 전달받은 `props`를 사용해 `<head>` 안에 동적으로 `<meta>` 태그를 생성한다.

저는 공통 레이아웃을 찾아보니, `src/layouts/BaseLayout.astro` 이었으며, 여기서 전달받은 Props를 `BaseHead`라는 컴포넌트로 Props를 통해 다시 전달하는 구조로 되어 있었습니다. 그리고 이 `BaseHead.astro`에 정의된 부분에서 이미 정의된 `<meta property="og:image">` 태그를 발견할 수 있었습니다.

```jsx
---
import "@styles/global.css"
import BaseHead from "@components/BaseHead.astro"
import Header from "@components/Header.astro"
import Footer from "@components/Footer.astro"
import Drawer from "@components/Drawer.astro"
const { title, description } = Astro.props
import { SITE } from "@consts"
---

<!doctype html>
<html lang="en">
  <head>
    <BaseHead title={`${title} | ${SITE.TITLE}`} description={description} />
  </head>
  <body>
    <Header />
    <Drawer />
    <main>
      <slot />
    </main>
    <Footer />
  </body>
</html>

```

```jsx
import { ViewTransitions } from "astro:transitions"

interface Props {
  title: string
  description: string
  image?: string
}

const canonicalURL = new URL(Astro.url.pathname, Astro.site)

const { title, description, image = "/public/logo.png" } = Astro.props
---

<!-- Global Metadata -->
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1" />
<link rel="icon" type="image/webp" href="/logo.webp" />
<meta name="generator" content={Astro.generator} />

<link rel="preload" href="/fonts/atkinson-regular.woff" as="font" type="font/woff" crossorigin>
<link rel="preload" href="/fonts/atkinson-bold.woff" as="font" type="font/woff" crossorigin>

<!-- Canonical URL -->
<link rel="canonical" href={canonicalURL} />

<!-- Primary Meta Tags -->
<title>{title}</title>
<meta name="title" content={title} />
<meta name="description" content={description} />

<!-- Open Graph / Facebook -->
<meta property="og:type" content="website" />
<meta property="og:url" content={Astro.url} />
<meta property="og:title" content={title} />
<meta property="og:description" content={description} />
<meta property="og:image" content={new URL(image, Astro.url)} />
```

따라서 props에 기본값으로 정의된 파일의 주소인 `"/open-graph.jpg”`를 제가 사용하려고 하는 이미지 파일의 주소인 `“/public/logo.png”` 교체했습니다. 이 때 무심결에 실제 경로인 `public`을 추가했습니다.

### 2. 404 Not Found

코드를 작성하고 사이트를 배포했지만, 오히려 공유를 해보니 이미지가 사려졌습니다. 파일을 찾지 못하고 있다고 의심 중에 개발자 도구를 통해 `og:image`의 URL을 얻어 직접 접속해보니 **404 Not Found 오류**가 발생했습니다. 

![사라진 opengraph img](./img/2025-07-04-image%201.png)

사라진 opengraph img

- 생각해본 오류
    - `git status`: 이미지 파일과 소스 코드가 Git에 잘 commit 되었는가? -> **문제없음**
    - `.gitignore`: 이미지 파일이 무시되고 있지는 않은가? -> **문제없음**
    - `astro.config.mjs`의 `base` 설정: GitHub Pages 배포 규칙에 맞는가?  -> **문제없음**
        - `<username>.github.io` 저장소는 `base` 설정 불필요

모든 것이 정상인 것 같았지만 문제는 해결되지 않았습니다. 그런데 **로컬 개발 환경**에서는 `og:image`의 URL로 직접 접속하니 404 Not Found가 아니고 잘 동작하는 것처럼 보였기에 더욱 혼란스러웠습니다.

### 3. `public` 폴더의 비밀

문제 해결의 실마리는 로컬과 배포 환경에서 테스트하던 URL의 미세한 차이에서 발견되었습니다.

- **로컬에서 동작했던 주소:** `http://localhost:4321/public/logo.png`
- **배포 후 404가 발생한 주소:** `https://seonrizee.github.io/public/logo.png`

**원인은 바로 경로에 포함된 `public`이었습니다.**

**Astro**는 빌드 시, `public` 폴더 **'자체'**를 동일하게 그대로 복사하는 것이 아니라, 그 **'안에 있는 내용물'**을 사이트의 **최상위(루트) 경로**로 그대로 옮깁니다.

- **개발 폴더:** `public/logo.png`
- **배포된 사이트:** `/logo.png`

따라서 코드에서 `public` 폴더 안의 파일을 참조할 때는, 경로에서 `public/` 부분을 완전히 빼고 루트에서 바로 시작하는 것처럼 작성해야 했습니다.

```jsx
const { title, description, image = "/public/logo.png" } = Astro.props // 수정 전
const { title, description, image = "/logo.png" } = Astro.props // 수정 후
```

이렇게 수정한 후 `https://seonrizee.github.io/logo.png`로 접속하니 이미지가 정상적으로 표시되었습니다. 그리고 최종적으로 [카카오에서 제공하는 공유 디버거](https://developers.kakao.com/tool/debugger/sharing)를 통해 정상적으로 출력하는 것을 확인했습니다. 

![카카오에서 제공하는 공유 디버거](./img/2025-07-04-image%202.png)

카카오에서 제공하는 공유 디버거

그리고 실제로 공유를 해보니 원하던 이미지가 정상적으로 출력된 것을 확인할 수 있었습니다.

![image.png](./img/2025-07-04-image%203.png)

### 4. 왜 이렇게 동작할까?

<aside>
💡

왜 이렇게 동작하는지 알려면 **public**폴더의 의미를 먼저 알아야 합니다. 

</aside>

`Rails`나 `express` 등은 public 폴더를 사용해 정적 파일을 제공하면서 소스 코드와 정적 파일을 분리하고자 하는 시도를 했습니다. 이렇게 하면 소스를 보호할 수 있고, 유지보수도 기존에 비해 용이했습니다. 그러다가 결정적으로 `Create React App` 에서 공식적으로 public 폴더를 채택하면서 **`public/` 폴더 → 빌드 시 `build/` 루트 복사**라는 개념이 프론트엔드 환경에서 완전히 자리잡기 시작했습니다. 사실 곰곰히 생각해보면 단순히 폴더 하나를 이름을 붙여서 추가했을 뿐인데, 이러한 역사가 자리잡고 있습니다.

마찬가지로 Astro 또한 public 폴더의 파일은 **루트 폴더**로 옮겨 진후 사이트가 빌드된다고 공식 문서에 나와있습니다. [`Astro`](https://docs.astro.build/ko/basics/project-structure/#public)의 공식 문서 중 관련 부분을 보면 **빌드 과정 중에 처리되거나 포함되지 않는 파일들 혹은 루트와 같은 반드시 특정 경로에 위치해야 하는 파일을 해당 경로에서 쉽게 관리**하기 위해 사용한다는 것을 알 수 있습니다. 따라서 루트 폴더로 옮겨질 것을 고려하여 코드에서는 `/public/logo.png` 가 아닌 `/logo.png`로 작성했어야 했습니다.

> `public/` 디렉터리는 Astro 빌드 과정 중에 처리할 필요가 없는 프로젝트의 파일과 자산을 위한 곳입니다. 이 폴더의 파일은 변경 없이 빌드 폴더로 복사된 후 사이트가 빌드됩니다.
이러한 동작으로 인해 `public/`은 일부 이미지와 글꼴, 또는 `robots.txt` 및 `manifest.webmanifest`와 같은 특수 파일과 같이 처리할 필요가 없는 일반적인 자산에 이상적입니다.
`public/` 디렉터리에 CSS와 JavaScript를 배치할 수 있지만, 해당 파일은 최종 빌드에서 번들링되거나 최적화되지 않습니다.
[-Astro 공식 문서](https://docs.astro.build/en/basics/project-structure/#public)
> 

하지만 **로컬 개발 서버**에서는  Astro에서 사용하는 도구인 `Vite`가 public 폴더의 파일들이 루트에 있는 것처럼 **매핑**을 합니다. 하지만 실제로 빌드했을 때처럼 **실제 파일을 옮기는 것은 아니기** 때문에 주소가 `/public/logo.png`로 작성되어 있어도 **실제 파일이 여전히 존재**하여 정상 작동했던 것이었습니다.

> 그런 다음 프로젝트 루트 아래의 특정 `public`디렉터리에 에셋을 저장할 수 있습니다. 이 디렉터리의 에셋은 개발 과정에서 루트 경로에 제공되고 `/`, dist 디렉터리의 루트에 그대로 복사됩니다.
[-Vite 공식 문서](https://vite.dev/guide/assets.html#the-public-directory)
> 

### 5. 게시글마다 다른 이미지 보여주기

지금까지는 모든 페이지에 동일한 기본 이미지로 설정하는 방법과 원리 그리고 역사를 알아봤습니다. 하지만 특정 게시글에는 그에 맞는 고유한 썸네일을 보여주고 싶을 수 있습니다. 이럴 땐 아래와 같이 코드를 확장할 수 있습니다.

먼저 이미지 주소 또한 ‘image’와 같은 이름의 Props로 전달하여 <head> 태그를 표현하는 BaseLayout 역할의 컴포넌트에서 전달받습니다.

그리고 아래 예제의 방식처럼 `‘??’(Nullish Coalescing 연산자)` 를 사용하여 간단하게 코드를 작성하면 됩니다. `??` 연산자는 왼쪽 값이 `null` 또는 `undefined`일 경우에만 오른쪽 값을 사용하라는 의미입니다. 이렇게 하면 ‘image’라는 Props가 존재하면 전달받은 Props의 주소를 사용하고, 없으면 기본값으로 설정한 이미지의 주소를 사용하게 됩니다.

```jsx
export interface Props {
	title: string;
	description: string;
	image?: string; // 페이지 전용 이미지는 선택사항입니다.
}

const { title, description, image } = Astro.props;

const DEFAULT_IMAGE = "/open-graph.png";
const ogImage = image ?? DEFAULT_IMAGE;
---

<meta property="og:image" content={new URL(ogImage, Astro.url)} />

```

이 글을 통해 `opengraph protocol`의 역할과 `public` 폴더의 의미와 역사 그리고 Astro의 빌드 프로세스 및 정적 파일 관리에 대해 이해할 수 있었습니다. 저와 비슷한 문제를 겪는 분들께 이 글이 도움이 되기를 바랍니다.

참고

- [Astro 공식 문서 - Project structure  > The Public Directory](https://docs.astro.build/en/basics/project-structure/#public)
- [Vite 공식 문서 - Static Asset handling > The public Directory](https://vite.dev/guide/assets.html#the-public-directory)
- [Create React App 공식 문서(deprecated) - 공용 폴더 사용](https://create-react-app.dev/docs/using-the-public-folder/)
- [Webpack 공식 문서 - Public Path](https://webpack.js.org/guides/public-path/)
- Google Gemini