import type { Site, Page, Links, Socials } from "@types";

// Global
export const SITE: Site = {
  TITLE: "seonrizee",
  DESCRIPTION: "Software Engineer",
  AUTHOR: "Pilseon Kim",
};

// Blog Page
export const BLOG: Page = {
  TITLE: "Blog",
  DESCRIPTION: "Writing on topics I am passionate about.",
};

// TIL Page
export const TIL: Page = {
  TITLE: "Today I Learned",
  DESCRIPTION: "Things I have learned.",
};

// Problem-solving Page
export const PS: Page = {
  TITLE: "Problem Solving",
  DESCRIPTION: "Recent Problems I have solved on.",
};

// Search Page
export const SEARCH: Page = {
  TITLE: "Search",
  DESCRIPTION: "Search all posts and projects by keyword.",
};

// Work Page
export const WORK: Page = {
  TITLE: "Work",
  DESCRIPTION: "Places I have worked.",
};

// Projects Page
export const PROJECTS: Page = {
  TITLE: "Projects",
  DESCRIPTION: "Recent projects I have worked on.",
};

// About Page
export const ABOUT: Page = {
  TITLE: "About",
  DESCRIPTION: "About me",
};

// Links
export const LINKS: Links = [
  {
    TEXT: "Home",
    HREF: "/",
  },
  {
    TEXT: "Blog",
    HREF: "/blog",
  },
  {
    TEXT: "TIL",
    HREF: "/til",
  },
  {
    TEXT: "Problem Solving",
    HREF: "/ps",
  },
  {
    TEXT: "About",
    HREF: "/about",
  },
];

// Socials
export const SOCIALS: Socials = [
  {
    NAME: "Email",
    ICON: "email",
    TEXT: "seonrizee@gmail.com",
    HREF: "mailto:seonrizee@gmail.com",
  },
  {
    NAME: "Github",
    ICON: "github",
    TEXT: "seonrizee",
    HREF: "https://github.com/seonrizee",
  },
  // {
  //   NAME: "LinkedIn",
  //   ICON: "linkedin",
  //   TEXT: "Pilseon Kim",
  //   HREF: "https://www.linkedin.com/in/pilseon-kim-83804a210",
  // },
  // {
  //   NAME: "Twitter",
  //   ICON: "twitter-x",
  //   TEXT: "seonrizee",
  //   HREF: "https://twitter.com/seonrizee",
  // },
];
