function Card({ children, className = '' }) {
  return (
    <section
      className={`rounded-[30px] border border-white/75 bg-white p-6 shadow-[0_18px_38px_rgba(14,47,103,0.08)] ${className}`}
    >
      {children}
    </section>
  );
}

export default Card;
